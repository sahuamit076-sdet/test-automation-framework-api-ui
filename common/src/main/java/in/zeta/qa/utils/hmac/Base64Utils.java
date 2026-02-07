package in.zeta.qa.utils.hmac;

import java.util.Base64;

public class Base64Utils {

    public static String encodeNoWrap(byte[] byteArray) {
        return Base64.getEncoder().encodeToString(byteArray);
    }

}
