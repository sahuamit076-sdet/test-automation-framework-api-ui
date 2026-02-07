package in.zeta.qa.testdata.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TxnPostActionData {
    @Deprecated
    private String action; // Mandatory
    private String txnAmount; // Mandatory
    private String expectedResponseCode; // Mandatory
    private String expectedErrorCode; // Optional
    private String mcc; // Optional
    private String date; // Optional
}
