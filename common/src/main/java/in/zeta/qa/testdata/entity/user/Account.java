package in.zeta.qa.testdata.entity.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class Account extends User {

    @JsonProperty("ACCOUNT_ID")
    @JsonAlias("account_id")
    protected String accountId;
    @JsonProperty("PRODUCT_ID")
    String productId;

    protected String walletId;
    protected String crn;

    protected String accountLedgerId;
    protected String systemLedgerId;

    @Override
    public Account clone() {
        return (Account) super.clone();
    }
}
