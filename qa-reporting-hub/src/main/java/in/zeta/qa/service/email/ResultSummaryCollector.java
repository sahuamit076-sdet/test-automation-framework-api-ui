package in.zeta.qa.service.email;

import in.zeta.qa.entity.allure.AllureReportSummary;
import in.zeta.qa.entity.email.EmailJobSummary;
import in.zeta.qa.entity.email.EmailSummary;
import in.zeta.qa.utils.fileUtils.YamlReader;
import in.zeta.qa.utils.jenkins.JenkinsUtil;
import in.zeta.qa.utils.misc.JsonHelper;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.*;

public class ResultSummaryCollector {
    static {
        System.setProperty("org.freemarker.loggerLibrary", "SLF4J");
    }

    private final JsonHelper jsonHelper = new JsonHelper();


    // ------------------------------------------------------------
    // Main Builder
    // ------------------------------------------------------------

    public static EmailSummary buildEmailSummary() {
        Map<String, Object> subscriptionData = loadSubscriptionYaml();

        List<EmailSummary.Section> sections = subscriptionData.entrySet().stream()
                .map(entry -> buildSection(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull).toList();

        EmailSummary emailSummary = new EmailSummary();
        emailSummary.setSubject("Automation Test Summary - " + LocalDate.now());
        emailSummary.setSections(sections);
        return emailSummary;
    }

    // ------------------------------------------------------------
    // YAML Loader
    // ------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadSubscriptionYaml() {
        Map<String, Object> values = new YamlReader().readYaml("subscription/email_subscription.yaml");
        Object subscriptionObj = values.get("subscription");

        if (!(subscriptionObj instanceof Map<?, ?> rawMap)) {
            throw new IllegalStateException("Expected 'subscription' to be a Map, got: " + subscriptionObj.getClass());
        }

        return (Map<String, Object>) rawMap;
    }

    // ------------------------------------------------------------
    // Section Builder
    // ------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static EmailSummary.Section buildSection(String sectionName, Object jobsObj) {
        if (!(jobsObj instanceof List<?> jobList)) return null;

        List<EmailJobSummary> jobs = jobList.stream()
                .map(job -> processJob((Map<String, Object>) job))
                .filter(Objects::nonNull).toList();

        EmailSummary.Section section = new EmailSummary.Section();
        section.setSection(sectionName);
        section.setDate(LocalDate.now().toString());
        section.setJobs(jobs);

        return section;
    }

    // ------------------------------------------------------------
    // Job Processor
    // ------------------------------------------------------------

    private static EmailJobSummary processJob(Map<String, Object> jobMap) {
        String jobName = (String) jobMap.get("job");
        String displayName = (String) jobMap.get("name");
        String env = (String) jobMap.get("environment");

        JenkinsUtil jenkinsUtil = new JenkinsUtil();
        Map<String, JenkinsUtil.BuildInfo> builds = jenkinsUtil.getTodayAndYesterdayBuilds(jobName);

        if (builds == null || builds.isEmpty()) {
            System.err.printf("⚠️ No builds found for job: %s%n", jobName);
            return null;
        }

        // Fetch today's summary
        JsonHelper jsonHelper = new JsonHelper();
        Response todayResp = jenkinsUtil.jenkinsAllureSummary(jobName, builds.get("today").number());
        AllureReportSummary todayAllure = jsonHelper.getObjectFromResponse(todayResp, AllureReportSummary.class);

        EmailJobSummary currentSummary = toEmailJobSummary(displayName, jobName, env, todayAllure, builds.get("today"));

        // Fetch yesterday's summary (if available)
        if (builds.get("yesterday") != null) {
            Response yestResp = jenkinsUtil.jenkinsAllureSummary(jobName, builds.get("yesterday").number());
            AllureReportSummary yestAllure = jsonHelper.getObjectFromResponse(yestResp, AllureReportSummary.class);

            EmailJobSummary previousSummary = toEmailJobSummary(displayName, jobName, env, yestAllure, builds.get("yesterday"));
            applyTrend(currentSummary, previousSummary);
        }

        return currentSummary;
    }

    // ------------------------------------------------------------
    // Conversion Helpers
    // ------------------------------------------------------------

    private static EmailJobSummary toEmailJobSummary(String displayName, String jobName, String env, AllureReportSummary allure, JenkinsUtil.BuildInfo buildInfo) {
        if (allure == null || allure.getStatistic() == null) {
            return createUnknownJob(displayName, jobName, env, buildInfo.number());
        }

        int passed = allure.getStatistic().getPassed();
        int failed = allure.getStatistic().getFailed();
        int broken = allure.getStatistic().getBroken();
        int total = allure.getStatistic().getTotal();

        int passPercent = calculatePassPercentage(passed, total);
        String status = determineStatus(passed, failed, broken, total);
        String duration = formatDuration(buildInfo.duration());

        return EmailJobSummary.builder()
                .name(displayName)
                .environment(env).status(status)
                .passPercentage(passPercent)
                .executionTime(duration)
                .reportLink(generateReportLink(jobName, buildInfo.number()))
                .build();
    }

    private static String formatDuration(long durationMs) {
        long hours = durationMs / 3_600_000;
        long remainingAfterHours = durationMs - (hours * 3_600_000);

        long minutes = remainingAfterHours / 60_000;
        long remainingAfterMinutes = remainingAfterHours - (minutes * 60_000);

        long seconds = remainingAfterMinutes / 1000;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("min ");
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append("sec");

        return sb.toString().trim();
    }

    private static EmailJobSummary createUnknownJob(String displayName, String jobName, String env, Integer buildId) {
        return EmailJobSummary.builder()
                .name(displayName)
                .environment(env)
                .status("UNKNOWN")
                .passPercentage(0)
                .executionTime("0 sec")
                .trend("UNKNOWN")
                .reportLink(generateReportLink(jobName, buildId))
                .build();
    }

    // ------------------------------------------------------------
    // Utility Methods
    // ------------------------------------------------------------

    private static String generateReportLink(String jobName, Integer buildId) {
        return String.format(
                "https://jenkins.internal.mum1-pp.zetaapps.in/job/%s/%s/allure/",
                jobName, buildId
        );
    }

    private static int calculatePassPercentage(int passed, int total) {
        if (total == 0) return 0;
        return (int) Math.round(((double) passed / total) * 100);
    }

    private static String determineStatus(int passed, int failed, int broken, int total) {
        return (failed == 0 && broken == 0 && passed == total) ? "PASS" : "FAIL";
    }

    private static void applyTrend(EmailJobSummary currentSummary, EmailJobSummary previousSummary) {
        if (previousSummary == null) {
            currentSummary.setTrend("NEW");
            return;
        }

        int current = currentSummary.getPassPercentage();
        int previous = previousSummary.getPassPercentage();

        if (current > previous) {
            currentSummary.setTrend("UP");
        } else if (current < previous) {
            currentSummary.setTrend("DOWN");
        } else {
            currentSummary.setTrend("SAME");
        }
    }



}
