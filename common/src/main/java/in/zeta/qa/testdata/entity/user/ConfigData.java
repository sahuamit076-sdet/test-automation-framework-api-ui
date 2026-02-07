package in.zeta.qa.testdata.entity.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ConfigData {
    @JsonProperty("ENVIRONMENT")
    @JsonAlias("environment")
    String env;
    @JsonProperty("IFI")
    @JsonAlias("tenant_id")
    String ifi;
}
