package in.zeta.qa.testdata.entity.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import in.zeta.qa.constants.Networks;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class Card extends Account {

    @JsonProperty("CARD_ID")
    @JsonAlias("CGUID")
    protected String cardId;
    @JsonProperty("CARD_NO")
    @JsonAlias({"no", "CARD_NAME"})
    protected String cardName;
    @JsonProperty("SLOT")
    @JsonAlias("slot_id")
    String slotId;

    protected String maskedPan;
    @JsonProperty("PAN")
    @JsonAlias("pan")
    protected String pan;
    @JsonProperty("EXPIRY")
    @JsonAlias("expiry")
    protected String expiry;

    @JsonProperty("CVV1")
    @JsonAlias("cvv1")
    protected String cvv1;
    @JsonProperty("CVV2")
    @JsonAlias("cvv2")
    protected String cvv2;
    @JsonProperty("ICVV")
    @JsonAlias("icvv")
    protected String icvv;
    @JsonProperty("PIN")
    @JsonAlias("pin")
    protected String pin;

    protected String status;
    protected String cardStatus;
    protected String cardType;

    protected String cardSeqNumber;
    protected String issuanceType;
    protected String issuedFrom;

    protected Networks network;

    @Override
    public Card clone() {
        return  (Card) super.clone();
    }
}
