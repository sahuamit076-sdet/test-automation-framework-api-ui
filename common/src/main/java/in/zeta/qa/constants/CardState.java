package in.zeta.qa.constants;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum CardState {

    // ---- Core States ----
    BLOCK(Status.DISABLED, Code.OTHER, "card is disabled"),
    DISABLED(Status.DISABLED, Code.ALLOCATED, "card activated"),
    INACTIVE(Status.INACTIVE, Code.SUSPENDED, "card suspended"),
    DELETED(Status.DELETED, Code.FRAUD_RISK, "fraud detected"),
    ACTIVE(Status.ACTIVE, Code.RESUMED, "card resumed"),

    // ---- Enabled ----
    ACTIVATED(Status.ENABLED, Code.ACTIVATED, "card enabled with reason code ACTIVATED"),
    RESUMED(Status.ENABLED, Code.RESUMED, "card resumed"),
    RISK_SUSPECTED(Status.ENABLED, Code.RISK_SUSPECTED, "card enabled with reason code RISK_SUSPECTED"),
    OTHER_ENABLED(Status.ENABLED, Code.OTHER, "card enabled with reason code OTHER"),

    // ---- Disabled ----
    SUSPENDED(Status.DISABLED, Code.SUSPENDED, "card is suspended"),
    PIN_BLOCKED(Status.DISABLED, Code.PIN_BLOCKED, "card disabled with reason code PIN_BLOCKED"),
    DORMANT(Status.DISABLED, Code.DORMANT, "card disabled with reason code DORMANT"),
    DEVICE_LOST(Status.DISABLED, Code.DEVICE_LOST, "card disabled with reason code DEVICE_LOST"),
    RTO(Status.DISABLED, Code.RTO, "card disabled with reason code RTO"),
    DELIVERY_STATUS_UNKNOWN(Status.DISABLED, Code.DELIVERY_STATUS_UNKNOWN, "card disabled with reason code DELIVERY_STATUS_UNKNOWN"),
    FRAUD_RISK_DISABLED(Status.DISABLED, Code.FRAUD_RISK, "card disabled with reason code FRAUD_RISK"),
    TRANSIT_RISK(Status.DISABLED, Code.TRANSIT_RISK, "card disabled with reason code TRANSIT_RISK"),
    DISPUTE(Status.DISABLED, Code.DISPUTE, "card disabled with reason code DISPUTE"),
    CLOSURE_INITIATED(Status.DISABLED, Code.CLOSURE_INITIATED, "card disabled with reason code CLOSURE_INITIATED"),
    OTHER_DISABLED(Status.DISABLED, Code.OTHER, "card disabled with reason code OTHER"),

    // ---- Deleted ----
    LOST(Status.DELETED, Code.LOST, "card lost"),
    STOLEN(Status.DELETED, Code.STOLEN, "card stolen"),
    DAMAGED(Status.DELETED, Code.DAMAGED, "card damaged"),
    OTHER_DELETED(Status.DELETED, Code.OTHER, "card deleted with reason code OTHER"),
    OFAC_BLOCKED(Status.DELETED, Code.OFAC_BLOCKED, "card deleted with reason code OFAC_BLOCKED"),
    FRAUD_RISK_DELETED(Status.DELETED, Code.FRAUD_RISK, "card deleted with reason code FRAUD_RISK"),
    SHREDDED(Status.DELETED, Code.SHREDDED, "card deleted with reason code SHREDDED"),
    REPLACED(Status.DELETED, Code.REPLACED, "card deleted with reason code REPLACED"),
    RENEWED(Status.DELETED, Code.RENEWED, "card deleted with reason code RENEWED"),
    REISSUED(Status.DELETED, Code.REISSUED, "card deleted with reason code REISSUED"),
    EXPIRED(Status.DELETED, Code.EXPIRED, "card deleted with reason code EXPIRED"),
    UNALLOCATED(Status.DELETED, Code.UNALLOCATED, "card deleted with reason code UNALLOCATED");

    private final Status status;
    private final Code code;
    private final String description;

    // ---- Lookup maps ----
    private static final Map<String, CardState> BY_NAME =
            Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(Enum::name, cs -> cs));
    private static final Map<String, CardState> BY_CODE =
            Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(cs -> cs.code.name(), cs -> cs, (a, b) -> a));

    public static Optional<CardState> fromString(String state) {
        return Optional.ofNullable(BY_NAME.get(state.toUpperCase(Locale.ROOT)));
    }

    public static Optional<CardState> fromCode(String code) {
        return Optional.ofNullable(BY_CODE.get(code.toUpperCase(Locale.ROOT)));
    }

    // ---- Nested enums to avoid duplicate strings ----
    public enum Status { ACTIVE, ENABLED, DISABLED, INACTIVE, DELETED }
    public enum Code {
        OTHER, ALLOCATED, SUSPENDED, FRAUD_RISK, RESUMED, ACTIVATED,
        PIN_BLOCKED, DORMANT, DEVICE_LOST, RTO, DELIVERY_STATUS_UNKNOWN,
        TRANSIT_RISK, DISPUTE, CLOSURE_INITIATED,
        LOST, STOLEN, DAMAGED, OFAC_BLOCKED, SHREDDED,
        REPLACED, RENEWED, REISSUED, EXPIRED, UNALLOCATED, RISK_SUSPECTED
    }
}
