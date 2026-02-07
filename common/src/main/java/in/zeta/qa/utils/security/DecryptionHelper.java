package in.zeta.qa.utils.security;

import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Iterator;

public class DecryptionHelper {

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static String decryptPluxeeFile(String encryptedFilePath, String privateKeyFilePath, String decryptPassword) throws Exception {
        return decryptFile(encryptedFilePath, privateKeyFilePath, decryptPassword);
    }


    public static String decryptFile(String encryptedFilePath, String privateKeyPath, String privateKeyPassword) throws Exception {
        InputStream encryptedData = new BufferedInputStream(new FileInputStream(encryptedFilePath));
        InputStream keyIn = new BufferedInputStream(new FileInputStream(privateKeyPath));

        // Load private key
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(
                PGPUtil.getDecoderStream(keyIn), new JcaKeyFingerprintCalculator());

        PGPObjectFactory pgpF = new PGPObjectFactory(PGPUtil.getDecoderStream(encryptedData), new JcaKeyFingerprintCalculator());
        Object o = pgpF.nextObject();
        if (o instanceof PGPEncryptedDataList) {
            // ok
        } else {
            o = pgpF.nextObject();
        }

        PGPEncryptedDataList enc = (PGPEncryptedDataList) o;

        Iterator<PGPEncryptedData> it = enc.getEncryptedDataObjects();
        PGPPrivateKey sKey = null;
        PGPPublicKeyEncryptedData pbe = null;

        while (it.hasNext()) {
            pbe = (PGPPublicKeyEncryptedData) it.next();
            PGPSecretKey pgpSecKey = pgpSec.getSecretKey(pbe.getKeyID());

            if (pgpSecKey != null) {
                sKey = pgpSecKey.extractPrivateKey(
                        new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(privateKeyPassword.toCharArray()));
                break;
            }
        }

        if (sKey == null) {
            throw new IllegalArgumentException("Private key for decryption not found.");
        }

        InputStream clear = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(sKey));
        PGPObjectFactory plainFact = new PGPObjectFactory(clear, new JcaKeyFingerprintCalculator());

        Object message = plainFact.nextObject();

        if (message instanceof PGPCompressedData) {
            PGPCompressedData compressedData = (PGPCompressedData) message;
            PGPObjectFactory pgpFact = new PGPObjectFactory(compressedData.getDataStream(), new JcaKeyFingerprintCalculator());
            Object innerMessage = pgpFact.nextObject();

            if (innerMessage instanceof PGPLiteralData) {
                PGPLiteralData literalData = (PGPLiteralData) innerMessage;
                InputStream unc = literalData.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                int ch;
                while ((ch = unc.read()) >= 0) {
                    out.write(ch);
                }

                return new String(out.toByteArray(), StandardCharsets.UTF_8);
            } else {
                throw new PGPException("Compressed data does not contain literal data.");
            }
        } else if (message instanceof PGPLiteralData) {
            PGPLiteralData literalData = (PGPLiteralData) message;
            InputStream unc = literalData.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int ch;
            while ((ch = unc.read()) >= 0) {
                out.write(ch);
            }

            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } else {
            throw new PGPException("Unknown message type.");
        }
    }
}
