package in.zeta.qa.constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface CommonConstants {

    String EXCLUDE_SUITE = "excludeSuite";
    String UAT_ZONE = "hdfclz_local";
    String SHOWROOM_ZONE = "showroom";
    String CARD_DATA = "Card Data";
    String TP_AUTOMATION_COLLECTION_ID = "TxnPodAutomationOTP";
    String TP_AUTOMATION_COLLECTION_NAME = "TxnPodAutomationOTP@collections.zeta.in";
    String CONTENT_TYPE = "Content-Type";
    String DOMAIN = "1000001-payzapp-users.in";
    String PUB_KEY = "public_key";
    String PLUS ="+";
    String PUBLIC_KEY_ENCODED = "publicKeyBase64Encoded";
    String PRIVATE_KEY_ENCODED = "privateKeyBase64Encoded";
    String CLIENT_ID = "foWfbQPwyvpdPPujCCQr.payzap";
    String CLIENT_SECRET = "Wy+171sdvICXqO+SYT51dgkIFj4ol+FMO/SXEsUlJkg=";
    String UTC_TIME_ZONE = "UTC";
    String COMMA = ",";
    String COLON = ":";
    String QUOTE = "\"";
    String UTF_8 = "UTF-8";
    String EMPTY_STRING = "";
    String POSTGRES_DRIVER = "org.postgresql.Driver";
    String[] FIELDS_TO_IGNORE_IN_BALANCES =  {"pendingAllocation", "excessCreditAllocatableBalance", "excessCreditUnallocatableBalance", "overlimitUsage", "remainingStatementedBalance", "remainingMinDueBalance", "asOfDate"};
    String APPLY = "APPLY";
    String REPAYMENT_EXTERNAL_REFERENCE_ID = "PGBD2702202311012231";
    String PG_CARD_DEBIT = "PG_CARD_DEBIT";
    String HDFC_OPS = "HDFC_OPS";
    String ACTIVE = "ACTIVE";
    String REPAYMENT = "REPAYMENT";
    String FAAFDFASFA = "faafdfasfa";
    String FASFDSAASDF = "fasfdsaasdf";

    Set<String> usCountries = new HashSet<>(Arrays.asList("USA", "US"));
    Set<String> inCountries = new HashSet<>(Arrays.asList("IND", "IN"));

    String ACCOUNT_HOLDER = "ACCOUNTHOLDER";
    String PAYMENT_ACCOUNT_URI = "paymentAccount://";
    String WALLET_URI = "wallet://";
    String DEFAULT_ATM_PIN = "1234";
    String REVERSAL = "REVERSAL";
    String CREDIT_UNDERSCORE = "CREDIT_";
    String DEBIT = "DEBIT";
}
