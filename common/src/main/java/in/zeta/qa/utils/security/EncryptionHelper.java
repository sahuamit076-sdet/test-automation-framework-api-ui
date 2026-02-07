package in.zeta.qa.utils.security;

import in.zeta.qa.utils.cuncurrency.SingletonFactory;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.stream.StreamSupport;

public class EncryptionHelper {

    private EncryptionHelper() {
        // private constructor to prevent instantiation
    }

    public static EncryptionHelper getInstance() {
        return SingletonFactory.getInstance(EncryptionHelper.class);
    }

    private static final Logger LOG = LogManager.getLogger(EncryptionHelper.class);
    private static final String GCM_TAG_SIZE = "128";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // ---------------------------
    // 🔐 PGP ENCRYPTION
    // ---------------------------

    private PGPPublicKey readPgpPublicKey(String fileName) throws IOException, PGPException {
        try (InputStream input = new BufferedInputStream(Files.newInputStream(Paths.get(fileName)))) {
            PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(
                    PGPUtil.getDecoderStream(input), new JcaKeyFingerprintCalculator());

            return StreamSupport.stream(pgpPub.spliterator(), false)
                    .flatMap(ring -> StreamSupport.stream(ring.spliterator(), false))
                    .filter(PGPPublicKey::isEncryptionKey)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No encryption key found in key ring"));
        }
    }

    private File encryptFileInternal(File file, String publicKeyPath) throws IOException, PGPException {
        byte[] literalData;
        PGPCompressedDataGenerator compressedGen = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);

        try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
            PGPUtil.writeFileToLiteralData(compressedGen.open(bOut), PGPLiteralData.BINARY, file);
            literalData = bOut.toByteArray();
        } finally {
            compressedGen.close(); // must close manually
        }

        PGPPublicKey pgpPublicKey = readPgpPublicKey(publicKeyPath);

        JcePGPDataEncryptorBuilder dataEncryptor = new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
                .setSecureRandom(new SecureRandom())
                .setProvider(BouncyCastleProvider.PROVIDER_NAME);

        PGPEncryptedDataGenerator encryptedGen = new PGPEncryptedDataGenerator(dataEncryptor);
        encryptedGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(pgpPublicKey)
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .setSecureRandom(new SecureRandom()));

        File encryptedFile = new File(file.getParent(), file.getName() + ".gpg");
        try (OutputStream out = Files.newOutputStream(encryptedFile.toPath());
             OutputStream encOut = encryptedGen.open(out, literalData.length)) {
            encOut.write(literalData);
        }
        return encryptedFile;
    }

    public File encryptFileUsingPgp(File file, String publicKeyPath) throws IOException, PGPException {
        return encryptFileInternal(file, publicKeyPath);
    }

    // ---------------------------
    // 🔐 RSA KEYS
    // ---------------------------


    @SneakyThrows
    private SecretKeySpec generateAESKey(PrivateKey selfPrivateKey, PublicKey selfPublicKey, PublicKey sharedPublicKey) {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "BC");
        keyAgreement.init(selfPrivateKey);
        keyAgreement.doPhase(sharedPublicKey, true);
        byte[] sharedSecret = keyAgreement.generateSecret();
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(sharedSecret);
        byte[] selfPublicKeyBytes = selfPublicKey.getEncoded();
        byte[] sharedPublicKeyBytes = sharedPublicKey.getEncoded();
        List<ByteBuffer> publicKeys = Arrays.asList(ByteBuffer.wrap(selfPublicKeyBytes), ByteBuffer.wrap(sharedPublicKeyBytes));
        Collections.sort(publicKeys);
        messageDigest.update(((ByteBuffer) publicKeys.get(0)).array(), 0, ((ByteBuffer) publicKeys.get(0)).array().length);
        messageDigest.update(((ByteBuffer) publicKeys.get(1)).array(), 0, ((ByteBuffer) publicKeys.get(1)).array().length);
        return new SecretKeySpec(messageDigest.digest(), "AES");
    }

    @SneakyThrows
    private byte[] generateCipherText(String sensitiveFieldsJSON, KeyPair keyPair, PublicKey publicKey, byte[] iv) {
        SecretKeySpec senderAesKey = this.generateAESKey(keyPair.getPrivate(), keyPair.getPublic(), publicKey);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(Integer.parseInt(GCM_TAG_SIZE), iv);
        cipher.init(1, senderAesKey, gcmParameterSpec);
        return cipher.doFinal(sensitiveFieldsJSON.getBytes(StandardCharsets.UTF_8));
    }

    public static PublicKey getRSAPublicKey(String encodedKey) throws Exception {
        try {
            byte[] byteKey = Base64.getDecoder().decode(encodedKey);
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePublic(x509EncodedKeySpec);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid base64 encoded key", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("RSA algorithm is not available", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Invalid Key Specification for RSA", e);
        }
    }

    public static PrivateKey getRSAPrivateKey(String encodedKey) {
        try {
            byte[] byteKey = Base64.getDecoder().decode(encodedKey);
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePrivate(pkcs8EncodedKeySpec);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid base64 encoded private key", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("RSA algorithm is not available", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Invalid private key specification for RSA", e);
        }
    }

    @SneakyThrows
    public String getHmacAuthData(String resourceJid,
                                  String serverPublicKeyBase64,
                                  String clientPrivateKeyBase64) {

        // Decode base64 keys
        byte[] clientPrivKeyBytes = Base64.getDecoder().decode(clientPrivateKeyBase64);
        byte[] serverPubKeyBytes = Base64.getDecoder().decode(serverPublicKeyBase64);

        // Load EC Private Key
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(clientPrivKeyBytes);
        PrivateKey clientPrivateKey = keyFactory.generatePrivate(privateKeySpec);

        // Load EC Public Key
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(serverPubKeyBytes);
        PublicKey serverPublicKey = keyFactory.generatePublic(publicKeySpec);

        // ECDH Key Agreement
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(clientPrivateKey);
        keyAgreement.doPhase(serverPublicKey, true);
        byte[] sharedSecret = keyAgreement.generateSecret();

        // Token
        String token = resourceJid + ":" + "1";
        byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);

        // HMAC-SHA256 (truncated to 8 bytes)
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(sharedSecret, "HmacSHA256");
        hmac.init(keySpec);
        byte[] fullHmac = hmac.doFinal(tokenBytes);
        byte[] truncatedHmac = new byte[8];
        System.arraycopy(fullHmac, 0, truncatedHmac, 0, 8);

        // VERSION + HMAC + TOKEN
        byte[] result = new byte[1 + 8 + tokenBytes.length];
        result[0] = 1; // VERSION
        System.arraycopy(truncatedHmac, 0, result, 1, 8);
        System.arraycopy(tokenBytes, 0, result, 9, tokenBytes.length);

        return Base64.getEncoder().encodeToString(result);
    }


    // ---------------- RSA Encryption ----------------
    @SneakyThrows
    public static String rsaEncrypt(String plainText, String publicKeyPEM) {
        PublicKey publicKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyPEM)));

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // ---------------- AES Encryption ----------------
    @SneakyThrows
    public static String aesEncrypt(String message, byte[] keyMaterial) {
        SecretKey aesKey = new SecretKeySpec(keyMaterial, "AES");

        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(new byte[16]); // static IV = zeros
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, iv);

        byte[] encrypted = aesCipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // ---------------- SHA-256 Hash ----------------
    @SneakyThrows
    public static byte[] sha256(String input) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input.getBytes(StandardCharsets.UTF_8));
    }


}



