package in.zeta.qa.testdata.entity;

import in.zeta.qa.testdata.entity.user.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionContext {

    private Card card;
    private Long systemTimeOfTransaction;
    private String channelCode;

    private String expectedErrorCode;
    private String expectedActionCode;

    private Boolean isFullyReversed = false;
    private Boolean isBalanceCheck = true;
    private Boolean isLedgerCheck = true;
}
