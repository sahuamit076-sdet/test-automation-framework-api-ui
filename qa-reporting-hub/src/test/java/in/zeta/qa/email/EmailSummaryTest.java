package in.zeta.qa.email;

import in.zeta.qa.BaseTest;
import in.zeta.qa.entity.email.EmailSummary;
import in.zeta.qa.service.email.EmailSummaryHtmlBuilder;
import in.zeta.qa.service.email.ResultSummaryCollector;
import in.zeta.qa.utils.misc.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.io.FileWriter;
import java.io.IOException;


@Slf4j
public class EmailSummaryTest extends BaseTest {

    @Test
    void  emailPublisher() {
        EmailSummary summary = ResultSummaryCollector.buildEmailSummary();
        String jsonSummary = new JsonHelper().convertObjectToJsonString(summary);
        log.info("Generated Email Summary: {}", jsonSummary);
        // Optional: choose output path
        String outputPath = "email-summary.json";
        try (FileWriter fileWriter = new FileWriter(outputPath)) {
            fileWriter.write(jsonSummary);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        EmailSummaryHtmlBuilder.buildHtml(summary);
    }


}
