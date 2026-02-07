package in.zeta.qa.entity.allure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllureReportSummary {

    private String reportName;
    private Statistic statistic;
    private Time time;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Statistic {
        private int failed;
        private int broken;
        private int skipped;
        private int passed;
        private int unknown;
        private int total;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Time {
        private long start;
        private long stop;
        private long duration;
        private long minDuration;
        private long maxDuration;
        private long sumDuration;
    }
}
