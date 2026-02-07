package in.zeta.qa.entity.email;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailJobSummary {
    private String name;
    private String environment;
    private String executionTime;
    private String status; // "PASSED", "FAILED", "UNKNOWN", etc.
    private Integer passPercentage;
    private String trend; // "UP", "DOWN", or "SAME"
    private String reportLink;

}
