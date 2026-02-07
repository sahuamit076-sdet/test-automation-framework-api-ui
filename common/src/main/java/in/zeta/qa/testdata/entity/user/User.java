package in.zeta.qa.testdata.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import in.zeta.qa.constants.CountryData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class User extends ConfigData implements Cloneable {

    @JsonProperty("ACCOUNT_HOLDER_ID")
    protected String accountHolderId;
    @JsonProperty("CUSTOMER_ID")
    protected String customerId;
    @JsonProperty("PHONE_NUMBER")
    protected String phoneNumber;
    @JsonProperty("COUNTRY")
    protected CountryData countryData;
    @JsonProperty("EMAIL_ID")
    protected String emailId;
    @JsonProperty("VPA")
    protected String vpa;
    @JsonProperty("PSP_USER_ID")
    protected String pspUserId;
    @JsonProperty("ACC_REF_NUM")
    private String accRefNum;


    @Override
    public User clone() {
        try {
            return (User) super.clone();  // includes parent fields (ConfigData)
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }
}
