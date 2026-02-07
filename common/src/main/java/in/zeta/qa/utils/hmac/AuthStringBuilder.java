package in.zeta.qa.utils.hmac;

public class AuthStringBuilder {

    private static final char AUTH_MESSAGE_SEPARATOR = '|';

    public static String forHmac(long requestId, long validUntil, String creditAddress, long amount) {
        return forHmac(String.valueOf(requestId), validUntil, creditAddress, amount);
    }

    public static String forHmac(String requestId, long validUntil, String creditAddress, long amount) {
        return new StringBuilder()
                .append(requestId)
                .append(AUTH_MESSAGE_SEPARATOR)
                .append(creditAddress)
                .append(AUTH_MESSAGE_SEPARATOR)
                .append(validUntil)
                .append(AUTH_MESSAGE_SEPARATOR)
                .append(amount)
                .toString();
    }
}
