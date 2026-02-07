package in.zeta.qa.entity.email;

import lombok.Data;

import java.util.List;

@Data
public class EmailSummary {

    private String subject;
    private List<Section> sections;

    @Data
    public static class Section {
        private String section;
        private String date;
        private List<EmailJobSummary> jobs;
    }
}
