package in.zeta.qa.utils.hmac;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HmacUtils {

    public static byte[] hmac(String tokenWithoutHMac, byte[] secret) throws NoSuchAlgorithmException, InvalidKeyException {
        return hmac(tokenWithoutHMac.getBytes(StandardCharsets.UTF_8), secret);
    }

    public static byte[] hmac(byte[] tokenWithoutHMac, byte[] secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSha256");
        SecretKey sharedKey = new SecretKeySpec(secret, "TlsPremasterSecret");
        mac.init(sharedKey);
        return mac.doFinal(tokenWithoutHMac);
    }
}
