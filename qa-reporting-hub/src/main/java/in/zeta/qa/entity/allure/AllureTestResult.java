package in.zeta.qa.entity.allure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AllureTestResult {

    @JsonProperty("name")
    private String name;
    @JsonProperty("status")
    private String status;
    @JsonProperty("labels")
    private List<NameValue> nameValues;
    @JsonProperty("parameters")
    private List<NameValue> parameters;
    @JsonProperty("statusDetails")
    private TestFailureReason failureReason;
    @JsonProperty("attachments")
    private List<RequestAttachments> attachments;
    private Long start;
    private Long stop;
    private String descriptionHtml;

    @JsonProperty("befores")
    private List<AllureTestResult> beforeMethod;



    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NameValue {
        private String name;
        private String value;
    }

}
