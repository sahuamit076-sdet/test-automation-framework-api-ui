package in.zeta.qa.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum Networks {
    MASTERCARD("Mastercard", ""),
    RUPAY("Rupay", "CHNINZZ1201"),
    VISA("Visa", "CHNINZZ1013"),
    PLUXEE("Pluxee", "CHNINZZ1201"),
    UPI_ON_RUPAY("UPI", "CHNINZZ2400"),
    PRIVATE_CHANNEL("Onus", "CHNINZZ5900"),
    GENERIC("Generic", "");

    private final String cardNetwork;
    private final String channelCode;

    private static final Map<String, Networks> LOOKUP =
            Stream.of(values()).collect(Collectors.toMap(Enum::name, e -> e));

    public static Networks fromString(String network) {
        if (Objects.isNull(network)) {
            return null;
        }
        Networks found = LOOKUP.get(network.toUpperCase());
        if (found == null) {
            throw new IllegalArgumentException("Unknown card network: " + network);
        }
        return found;
    }
}
