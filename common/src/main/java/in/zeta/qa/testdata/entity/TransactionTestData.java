package in.zeta.qa.testdata.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import in.zeta.qa.constants.anotation.PostActionMapping;
import in.zeta.qa.utils.customdeserializer.StringListDeserializer;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@PostActionMapping(TxnPostActionData.class)
public class TransactionTestData {

    //################################### META DATA FOR TESTING ##############################//
    @JsonProperty("RUN")
    private Boolean run;
    @JsonProperty("TEST_GROUP")
    @JsonDeserialize(using = StringListDeserializer.class)
    private List<String> testGroup;
    @JsonProperty("BANK_TEST_CASE_ID")
    private String bankTestCase;
    @JsonProperty("TACHYON_TEST_CASE_ID")
    private String tachyonTestCaseId;
    @JsonProperty("SCENARIO")
    private String scenario;

    //################################### CARD DATA ##############################//
    @JsonProperty("CARD")
    private String cardName;  // Mandatory
    @JsonProperty("ACCOUNT_STATUS")
    private String accountStatus;
    @JsonProperty("CARD_STATUS")
    private String cardStatus;
    @JsonProperty("TXN_STATUS")
    private String txnStatus;
    @JsonProperty("PAN")
    private String panData;
    @JsonProperty("CVV")
    private String cvvData;
    @JsonProperty("PIN")
    private String pinData; // Mandatory
    @JsonProperty("EXPIRY")
    private String expiryData;
    @JsonProperty("CAVV")
    private String cavvData;
    @JsonProperty("CARD_NETWORK")
    private String cardNetwork;
    @JsonProperty("AUTH_DATE_DIFF")
    private String authDateDiffer;


    //################################### OTHERS ##############################//
    @JsonProperty("DATE")
    private String dateInEpochMillis;
    @JsonProperty("IS_TOKENIZED")
    private String isTokenized;
    @JsonProperty("IS_LEDGER_CHECK")
    private String isLedgerCheck;
    @JsonProperty("IS_BALANCE_CHECK")
    private String isBalanceCheck;
    @JsonProperty("IS_ERROR_CODE_CHECK")
    private String isErrorCodeCheck;
    @JsonProperty("REMARK")
    private String remark;
    @JsonProperty("KAFKA_CHECK")
    private Boolean kafkaCheck;

    //################################### OTHER DE FIELDS ##############################//
    @JsonProperty("MCC_CODE")
    private String mccCode;
    @JsonProperty("COUNTRY_CODE")
    private String countryCode;
    @JsonProperty("CURRENCY_CODE")
    private String currencyCode;
    @JsonProperty("BILLING_CURRENCY_CODE")
    private String billingCurrencyCode;
    @JsonProperty("MERCHANT_NAME")
    private String merchantName;
    @JsonProperty("MERCHANT_ID")
    @JsonAlias("MID")
    private String merchantId;
    @JsonProperty("TERMINAL_ID")
    @JsonAlias("TID")
    private String terminalId;
    @JsonProperty("ACQUIRER_ID")
    private String acquirerId;
    @JsonProperty("MERCHANT_CITY")
    private String merchantCity;
    @JsonProperty("MERCHANT_STATE")
    private String merchantState;

    //################################### AMOUNTS ##############################//
    @JsonProperty("AMOUNT")
    private String txnAmount; // Mandatory

    //################################### ACTIONS SPECIFIC TO DATA READING FOR TESTS ##############################//
    @JsonProperty("ACTION")
    private String action;

    private List<TxnPostActionData> postActions;

    //################################### EXPECTED DATA FOR ASSERTION ##############################//
    @JsonProperty("RESPONSE_CODE")
    private String expectedResponseCode; // Mandatory
    @JsonProperty("ERROR_CODE")
    private String expectedErrorCode;
    @JsonProperty("RESPONSE_MESSAGE")
    private String expectedResponseMessage;
    @JsonProperty("EXPECTED_SUBCATEGORY")
    private String expectedSubCategory;
    @JsonProperty("PAYMENT_CODE")
    private String expectedPaymentCode;
    private String expectedTxnStatus;
    @JsonProperty("EXPECTED_STATUS")
    private String expectedStatus;


}
