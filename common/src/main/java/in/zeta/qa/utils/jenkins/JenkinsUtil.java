package in.zeta.qa.utils.jenkins;

import com.fasterxml.jackson.databind.JsonNode;
import in.zeta.qa.constants.anotation.RetryOnFailure;
import in.zeta.qa.constants.endpoints.JenkinsEndpoints;
import in.zeta.qa.utils.misc.AssertHelper;
import in.zeta.qa.utils.misc.CommonUtilities;
import in.zeta.qa.utils.misc.JsonHelper;
import in.zeta.qa.utils.rest.ApiRequest;
import in.zeta.qa.utils.rest.ApiResponse;
import in.zeta.qa.utils.rest.HttpMethod;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.testng.Assert;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
public class JenkinsUtil {

    private final JsonHelper jsonHelper = new JsonHelper();
    private final AssertHelper assertHelper = new AssertHelper();

    private static final ConcurrentHashMap<String, ReentrantLock> LOCKS = new ConcurrentHashMap<>();
    private static final String CONFIGS = "./src/main/resources/jenkins/configs.json";

    private record JenkinsConfig(String host, String path, String jobToken, String user, String password) {
    }

    private JenkinsConfig getInstanceConfig(String instance) {
        JsonNode config = jsonHelper.convertToJsonNodeFromFile(CONFIGS).get(instance);
        if (config == null) {
            log.error("Config for instance '{}' not found", instance);
            return null;
        }
        return new JenkinsConfig(
                config.path("host").asText(),
                config.path("path").asText(),
                config.path("job_token").asText(),
                config.path("user").asText(),
                config.path("password").asText()
        );
    }

    private void executeCurlJob(JenkinsConfig config, String cURL) {
        ReentrantLock lock = LOCKS.computeIfAbsent(config.path(), k -> new ReentrantLock());
        lock.lock();
        try {
            Map<String, Object> map = Collections.singletonMap("CURL", cURL);
            ApiResponse response = jenkinsBuildWithParams(config, map);
            assertHelper.validateStatusCode(response, HttpStatus.SC_CREATED);
            CommonUtilities.waitInSeconds(3);
        } finally {
            lock.unlock();
        }
    }

    public void executeCurlInJenkins(String instance, String cURL) {
        JenkinsConfig config = getInstanceConfig(instance);
        if (config != null) {
            executeCurlJob(config, cURL);
        }
    }

    public Optional<String> executeCurlInGetConsoleOutput(String instance, String cURL) {
        JenkinsConfig config = getInstanceConfig(instance);
        if (config == null) return Optional.empty();

        executeCurlJob(config, cURL);
        CommonUtilities.waitInSeconds(10);

        ReentrantLock lock = LOCKS.computeIfAbsent(config.path(), k -> new ReentrantLock());
        lock.lock();
        try {
            ApiResponse response = jenkinsJobInfo(config);
            assertHelper.validateStatusCode(response, HttpStatus.SC_OK);

            int buildId = jsonHelper.convertToJsonNode(response)
                    .path("lastSuccessfulBuild").path("number").asInt();

            response = jenkinsJobBuildConsoleText(config, buildId);
            return Optional.ofNullable(response.getBody());
        } finally {
            lock.unlock();
        }
    }

    public Optional<String> executeCurlInGetResponse(String instance, String cURL) {
        return executeCurlInGetConsoleOutput(instance, cURL)
                .map(this::extractResponse);
    }

    private String extractResponse(String consoleText) {
        String[] parts = consoleText.split("(?m)^\\s*100\\b.*--:--:--.*$", -1);
        String extractedResp = parts.length >= 2 ? parts[parts.length - 1] : "{}";
        log.info("CONSOLE OUTPUT :: {}", extractedResp);
        Assert.assertTrue(jsonHelper.isValidJson(extractedResp), "Extracted response is not valid JSON");
        return extractedResp;
    }

    //##################################################################################################################
    //############################################# API CALLs ##########################################################
    //###################################################################################################################

    @SneakyThrows
    @RetryOnFailure(count = 2, delayInSeconds = 3, statusCodes = {502, 404})
    private ApiResponse jenkinsBuildWithParams(JenkinsConfig config, Map<String, Object> formParams) {
        return ApiRequest.<Map<String, Object>>builder()
                .serverURL(config.host())
                .endpoint(JenkinsEndpoints.BUILD_WITH_PARAM)
                .pathParams(Map.of("PATH", config.path(), "TOKEN", config.jobToken()))
                .method(HttpMethod.POST)
                .username(config.user())
                .password(config.password())
                .formParams(formParams)
                .build().execute();
    }

    @SneakyThrows
    @RetryOnFailure(count = 2, delayInSeconds = 5, statusCodes = {502})
    private ApiResponse jenkinsJobInfo(JenkinsConfig config) {
        return ApiRequest.<Void>builder()
                .serverURL(config.host())
                .endpoint(JenkinsEndpoints.GET_BUILD_INFO)
                .pathParams(Map.of("PATH", config.path(), "TOKEN", config.jobToken()))
                .method(HttpMethod.GET)
                .username(config.user())
                .password(config.password())
                .build().execute();
    }

    @SneakyThrows
    @RetryOnFailure(count = 2, delayInSeconds = 5, statusCodes = {502})
    private ApiResponse jenkinsJobBuildConsoleText(JenkinsConfig config, int jobId) {
        CommonUtilities.waitInSeconds(2);
        return ApiRequest.<Void>builder()
                .serverURL(config.host())
                .endpoint(JenkinsEndpoints.GET_BUILD_CONSOLE_TEXT)
                .pathParams(Map.of("PATH", config.path(), "JOB_ID", String.valueOf(jobId), "TOKEN", config.jobToken()))
                .method(HttpMethod.GET)
                .username(config.user())
                .password(config.password())
                .build().execute();
    }

    @SneakyThrows
    @RetryOnFailure(count = 2, delayInSeconds = 5, statusCodes = {502})
    public ApiResponse jenkinsAllureSummary(String jobname, Integer buildId) {
        JenkinsConfig config = getInstanceConfig("itp-showroom");
        CommonUtilities.waitInSeconds(2);
        return ApiRequest.<Void>builder()
                .serverURL(config.host())
                .endpoint(JenkinsEndpoints.GET_ALLURE_REPORTS_BY_JOB_ID)
                .pathParams(Map.of("JOB_NAME", jobname, "BUILD_ID", String.valueOf(buildId)))
                .method(HttpMethod.GET)
                .username(config.user())
                .password(config.password())
                .build().execute();
    }

    @SneakyThrows
    @RetryOnFailure(count = 2, delayInSeconds = 5, statusCodes = {502})
    private ApiResponse getBuilds(String jobname) {
        JenkinsConfig config = getInstanceConfig("itp-showroom");
        CommonUtilities.waitInSeconds(2);
        return ApiRequest.<Void>builder()
                .serverURL(config.host())
                .endpoint(JenkinsEndpoints.GET_BUILDS)
                .pathParams(Map.of("JOB_NAME", jobname))
                .method(HttpMethod.GET)
                .username(config.user())
                .password(config.password())
                .build().execute();
    }

    //##########################################################################################
    //##########################################################################################
    public Map<String, BuildInfo> getTodayAndYesterdayBuilds(String jobname) {
        ApiResponse response = getBuilds(jobname);
        try {
            JsonNode root = jsonHelper.convertToJsonNode(response);
            JsonNode builds = root.get("builds");
            if (builds == null || !builds.isArray() || builds.isEmpty()) {
                return Collections.emptyMap();
            }

            // Filter builds triggered by timer
            Stream<JsonNode> filteredBuilds = filterBuildsTriggeredBy(builds);
            // Convert all builds to (number, timestamp) pairs
            List<BuildInfo> buildList = extractBuildInfo(filteredBuilds);
            // Group by date (yyyy-MM-dd in local timezone)
            Map<String, List<BuildInfo>> byDate = buildList.stream()
                    .collect(Collectors.groupingBy(b -> formatDate(b.timestamp())));

            Map<String, BuildInfo> result = new HashMap<>();
            // Find latest build for today
            byDate.entrySet().stream()
                    .filter(e -> e.getKey().equals(LocalDate.now().toString()))
                    .findFirst()
                    .ifPresent(e -> result.put("today", e.getValue().get(0)));

            // Find latest build for yesterday or earliest previous day
            byDate.entrySet().stream()
                    .sorted(Map.Entry.<String, List<BuildInfo>>comparingByKey().reversed()) // Sort by date descending
                    .filter(e -> LocalDate.parse(e.getKey()).isBefore(LocalDate.now()))
                    .findFirst().ifPresent(e -> result.put("yesterday", e.getValue().get(0)));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Jenkins build response", e);
        }
    }


    private Stream<JsonNode> filterBuildsTriggeredBy(JsonNode builds) {
        return StreamSupport.stream(builds.spliterator(), false)
                .filter(build -> {
                    JsonNode actions = build.get("actions");
                    if (actions == null || !actions.isArray()) return false;

                    for (JsonNode action : actions) {
                        JsonNode causes = action.get("causes");
                        if (causes != null && causes.isArray()) {
                            for (JsonNode cause : causes) {
                                JsonNode desc = cause.get("shortDescription");
                                if (desc != null && desc.asText().contains("Started by timer")) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                });
    }

    private List<BuildInfo> extractBuildInfo(Stream<JsonNode> builds) {
        List<BuildInfo> buildList = new ArrayList<>();
        builds.forEach(node -> buildList.add(new BuildInfo(
                node.get("number").asInt(),
                node.get("timestamp").asLong(),
                node.get("duration").asLong())));
        // Sort by timestamp descending
        buildList.sort(Comparator.comparingLong(BuildInfo::timestamp).reversed());
        return buildList;
    }

    private String formatDate(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public record BuildInfo(int number, long timestamp, long duration) {
    }

}
