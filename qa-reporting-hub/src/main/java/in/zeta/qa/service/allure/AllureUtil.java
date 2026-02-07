package in.zeta.qa.service.allure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import static in.zeta.qa.constants.ReportConstant.*;

import in.zeta.qa.constants.FilePath;
import in.zeta.qa.entity.allure.AllureTestResult;
import in.zeta.qa.entity.allure.ErrorMessage;
import in.zeta.qa.entity.allure.TestFailureReason;
import in.zeta.qa.service.kibana.KibanaService;
import in.zeta.qa.utils.cuncurrency.SingletonFactory;
import in.zeta.qa.utils.fileUtils.FileReadHelper;
import in.zeta.qa.utils.misc.JsonHelper;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.*;

public class AllureUtil implements FilePath {

    private AllureUtil() {
        // private constructor to prevent instantiation
    }

    public static AllureUtil getInstance() {
        return SingletonFactory.getInstance(AllureUtil.class);
    }

    private static final Logger LOG = LogManager.getLogger(AllureUtil.class);
    private static List<AllureTestResult> pastResults;

    private static final String JSON_EXT = ".json";
    private static final String RESULT_JSON = "result.json";
    private static final String CONTAINER_JSON = "container.json";
    private static final String CATEGORIES_JSON = "/allure/tp_categories.json";
    private static final String FAILED = "failed";
    private static final String SKIPPED = "skipped";
    private static final String NAME = "name";
    private static final String SUITE = "suite";
    private static final String CLUSTER = "CLUSTER";
    private static final String LINKS = "links";
    private static final String COMMA = ",";
    private static final String DESC = "descriptionHtml";


    private final JsonHelper jsonHelper = new JsonHelper();
    private final KibanaService kibanaService = new KibanaService();


    @SneakyThrows
    public List<AllureTestResult> readAllureResults(String resultPath) {
        LOG.info("Read Allure results {}", resultPath);
        Path path = Paths.get(resultPath);

        if (!Files.exists(path)) return new ArrayList<>();
        try (Stream<Path> walk = Files.walk(path)) {
            List<String> resultJsons = walk.filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString().toLowerCase()).filter(f -> f.endsWith(JSON_EXT)).collect(Collectors.toList());

            List<AllureTestResult> testResults = resultJsons.stream().filter(f -> f.endsWith(RESULT_JSON))
                    .map(this::readResultJson).filter(Objects::nonNull)
                    .filter(r -> Objects.nonNull(r.getAttachments()) && !r.getAttachments().isEmpty())
                    .collect(Collectors.toList());

            List<AllureTestResult> testResultsSetupFailures = resultJsons.stream().filter(f -> f.endsWith(CONTAINER_JSON))
                    .map(this::readResultJson).filter(Objects::nonNull)
                    .filter(r -> Objects.nonNull(r.getBeforeMethod()) && !r.getBeforeMethod().isEmpty())
                    .map(r -> r.getBeforeMethod().get(0)).filter(s -> !s.getStatus().equalsIgnoreCase("passed"))
                    .collect(Collectors.toList());
            testResults.addAll(testResultsSetupFailures);
            return testResults;
        }
    }

    @SneakyThrows
    public List<AllureTestResult> readCurrentAllureResults() {
        return readAllureResults(ALLURE_RESULT);
    }

    @SneakyThrows
    private AllureTestResult readResultJson(String testResultJsonFile) {
        String jsonResult = FileUtils.readFileToString(new File(testResultJsonFile), StandardCharsets.UTF_8);
        if (jsonResult.isEmpty()) return null;
        return jsonHelper.getObjectFromString(jsonResult, AllureTestResult.class);
    }

    @SneakyThrows
    @Deprecated
    public void generateCategoriesJsonFile(String filePath) {
        File catJson = new File(FileReadHelper.getFilesFromDirectory(filePath).get(0).toString());
        FileInputStream inputStream = new FileInputStream(catJson);
        Scanner sc = new Scanner(inputStream);
        StringBuilder buffer = new StringBuilder();
        while (sc.hasNext()) {
            buffer.append(sc.nextLine());
        }
        File catJsonRuntime = new File(Paths.get(ALLURE_RESULT) + CATEGORIES_JSON);
        FileWriter writer = new FileWriter(catJsonRuntime);
        writer.write(buffer.toString());
        writer.flush();
    }

    @SneakyThrows
    public void createCategoriesJsonFileAndOverride() {
        LOG.info("Updating tp_categories.json file ...");
        LOG.info("STEP1: EXTRACT ALL ALLURE ERROR/FAILURE MESSAGE");
        List<String> errorMessages = extractUniqueFailureMessages();
        LOG.info("STEP2: EXTRACT ERROR/FAILURE CATEGORY");
        List<String> uniqueFailureCategories = errorMessages.stream().map(this::extractFailureCategory)
                .distinct().filter(category -> !category.isEmpty()).collect(Collectors.toList());
        LOG.info("STEP3: GENERATE FAILURE CATEGORY JSON");
        String json = generateCategoriesJsonFromFailureCategories(uniqueFailureCategories);
        LOG.info("UPDATE CATEGORIES.JSON FILE IN ALLURE RESULTS");
        File catJsonRuntime = new File(Paths.get(ALLURE_RESULT) + CATEGORIES_JSON);
        FileWriter writer = new FileWriter(catJsonRuntime);
        writer.write(json);
        writer.flush();
    }

    private List<String> extractUniqueFailureMessages() {
        List<AllureTestResult> results = readCurrentAllureResults();
        return results.isEmpty() ? new ArrayList<>() :
                results.stream().filter(a -> Objects.nonNull(a.getFailureReason()))
                        .map(a -> a.getFailureReason().getMessage())
                        .filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());
    }

    private String extractFailureCategory(String failureMsg) {

        Pattern pattern1 = Pattern.compile("(.*?)" + HYPHEN_ARROW);
        Pattern pattern2 = Pattern.compile("(.+?)\\b" + "expected" + "\\b");
        Pattern pattern3 = Pattern.compile("(.*?)" + HYPHEN);

        String[] assertionPoints = failureMsg.split(",\\s*");
        for (String assertionMsg : assertionPoints) {
            Matcher matcher1 = pattern1.matcher(assertionMsg);
            Matcher matcher2 = pattern2.matcher(assertionMsg);
            Matcher matcher3 = pattern3.matcher(assertionMsg);

            if (matcher1.find()) {
                return matcher1.group(1).trim();
            } else if (matcher3.find()) {
                return matcher3.group(1).trim();
            } else if (matcher2.find()) {
                return matcher2.group(1).trim();
            } else {
                return failureMsg;
            }

        }
        return "";
    }


    private String generateCategoriesJsonFromFailureCategories(List<String> uniqueFailureCategories) throws Exception {
        LOG.info("Updating categories json file...");
        JsonHelper jsonHelper = new JsonHelper();
        List<ErrorMessage> errorMessages = jsonHelper.getObjectsFromJsonFile(ATHENA_REPORT_CATEGORIES_JSON, ErrorMessage.class);
        uniqueFailureCategories.forEach(e -> {
            ErrorMessage msg = new ErrorMessage();
            msg.setName(StringUtils.capitalize(e));
            msg.setMessageRegex(".*" + Pattern.quote(e) + ".*");
            errorMessages.add(msg);
        });
        return jsonHelper.convertObjectToJsonString(errorMessages);
    }

    //##################################################################################################################
    //##################################################################################################################

    @SneakyThrows
    public void optimizeNewFailures(boolean markFailureCategory, boolean searchKibanaLogs, boolean markSkippedAsFailed) {
        LOG.info("OPTIMIZING REPORT... ");
        try (Stream<Path> walk = Files.walk(Paths.get(ALLURE_RESULT))) {
            List<Path> resultJsonPaths = walk.filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.toString().toLowerCase().endsWith(RESULT_JSON))
                    .collect(Collectors.toList());

            for (Path path : resultJsonPaths) {
                AllureTestResult currentTestCase = readResultJson(path.toString());
                if (Objects.isNull(currentTestCase)) continue;

                if (currentTestCase.getStatus().equals(FAILED) && currentTestCase.getFailureReason() != null) {
                    Optional<AllureTestResult> oldFailedTestCase = findPastFailedTestWithSameError(currentTestCase);
                    if (markFailureCategory) {
                        LOG.info("marking failures...");
                        markNewAndExistingFailures(path, oldFailedTestCase);
                    }
                    if (searchKibanaLogs && !oldFailedTestCase.isPresent()) {
                        LOG.info("Adding kibana error logging ...");
                        linkKibanaErrorLog(path, currentTestCase);
                    }
                }

                if (markSkippedAsFailed && currentTestCase.getStatus().equals(SKIPPED)) {
                    updateStatusAsFailed(path);
                }

            }
        }
    }

    private void extractOldFailures() {
        try {
            if (pastResults == null)
                pastResults = readAllureResults(PAST_ALLURE_RESULT);
        } catch (Exception e) {
            LOG.error("No past result to compare");
        }
    }

    private Optional<AllureTestResult> findPastFailedTestWithSameError(AllureTestResult currentTestCase) {
        extractOldFailures();

        String currentName = Optional.ofNullable(currentTestCase.getName()).map(v -> v.replaceAll("[❌⚠️]", "")).orElse("");
        String currentMessage = Optional.ofNullable(currentTestCase.getFailureReason()).map(TestFailureReason::getMessage).orElse("");
        String currentSuiteName = Optional.ofNullable(currentTestCase.getNameValues())
                .flatMap(values -> values.stream().filter(l -> l.getName().equals(SUITE)).findFirst()
                        .map(AllureTestResult.NameValue::getValue)).orElse(null);

        Predicate<AllureTestResult> filterByNameAndError = pastTestCases -> {
            String pastName = Optional.ofNullable(pastTestCases.getName()).map(v -> v.replaceAll("[❌⚠️]", "")).orElse("");
            String pastMessage = Optional.ofNullable(pastTestCases.getFailureReason()).map(TestFailureReason::getMessage).orElse("");
            return currentName.equals(pastName) && FAILED.equals(pastTestCases.getStatus()) && currentMessage.equals(pastMessage);
        };


        Predicate<AllureTestResult> filterBySuite = pastTestCases -> {
            String pastSuiteName = Optional.ofNullable(pastTestCases.getNameValues())
                    .flatMap(values -> values.stream().filter(l -> l.getName().equals(SUITE)).findFirst()
                            .map(AllureTestResult.NameValue::getValue)).orElse(null);
            return currentSuiteName == null || pastSuiteName == null || currentSuiteName.equals(pastSuiteName);
        };

        return Optional.ofNullable(pastResults).orElse(Collections.emptyList())
                .stream().filter(filterByNameAndError.and(filterBySuite)).findFirst();
    }


    private void markNewAndExistingFailures(Path path, Optional<AllureTestResult> oldFailedTestCase) {
        ObjectMapper mapper = jsonHelper.getMapper();
        try {
            JsonNode root = mapper.readTree(path.toFile());
            if (root instanceof ObjectNode) {
                ObjectNode objectNode = (ObjectNode) root;
                String originalTestName = objectNode.get(NAME).asText();
                if (originalTestName.contains("❌") || originalTestName.contains("⚠️")) {
                    LOG.debug("Test already marked, skipping: {}", originalTestName);
                    return;
                }
                String testName = originalTestName.replaceAll("[❌⚠️]", "");
                if (!oldFailedTestCase.isPresent()) {
                    objectNode.put(NAME, "❌" + testName);
                    objectNode.put(DESC, "<b style='color:#C70039;'>❌ NEW FAILURE DETECTED</b>");
                } else {
                    objectNode.put(NAME, "⚠️" + testName);
                    String existingDescription = oldFailedTestCase.get().getDescriptionHtml();
                    int count = 1;
                    if (StringUtils.isNotEmpty(existingDescription)) {
                        Pattern pattern = Pattern.compile("LAST (\\d+) RUN");
                        Matcher matcher = pattern.matcher(existingDescription);
                        if (matcher.find()) {
                            count = Integer.parseInt(matcher.group(1)) + 1;
                        }
                    }
                    String updatedDescription = String.format("<b style='color:#FF5733;'>⚠️ FAILED WITH SAME ERROR FROM LAST %d RUN%s</b>", count, count > 1 ? "S" : "");
                    objectNode.put(DESC, updatedDescription);
                }
                String jsonString = mapper.writeValueAsString(objectNode);
                //TODO(Amit): after java upgrade Files.write(path, jsonString, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                Files.write(path, jsonString.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace(); // or better: use proper logging
        }
    }

    @SneakyThrows
    private void linkKibanaErrorLog(Path path, AllureTestResult currentTestCase) {
        List<String> clusters = currentTestCase.getParameters().stream()
                .filter(param -> param.getName().equalsIgnoreCase(CLUSTER))
                .findFirst().map(p -> Arrays.asList(p.getValue().split(COMMA)))
                .orElseGet(ArrayList::new);
        if (!clusters.isEmpty()) {
            String url = kibanaService.getKibanaUrlForErrorLogs(clusters, currentTestCase.getStart(), currentTestCase.getStop());
            if (StringUtils.isNotEmpty(url)) {
                ObjectMapper mapper = jsonHelper.getMapper();
                JsonNode root = mapper.readTree(path.toFile());
                if (root instanceof ObjectNode objectNode) {
                    ObjectNode kibanaLink = mapper.createObjectNode();
                    kibanaLink.put("name", "\uD83D\uDCCA KIBANA ERROR LOGS");
                    kibanaLink.put("type", "link");
                    kibanaLink.put("url", url);
                    ArrayNode existingLinks = (ArrayNode) root.get(LINKS);
                    if (Objects.isNull(existingLinks)) {
                        existingLinks = mapper.createArrayNode();
                    } else {
                        boolean alreadyExists = StreamSupport.stream(existingLinks.spliterator(), false)
                                .filter(link -> link.has("url"))
                                .anyMatch(link -> url.equals(link.get("url").asText()));
                        if (alreadyExists) return;
                    }
                    existingLinks.add(kibanaLink);
                    objectNode.set(LINKS, existingLinks);
                    String jsonString = mapper.writeValueAsString(objectNode);
                    Files.writeString(path, jsonString, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }
            }
        }
    }

    @SneakyThrows
    private void updateStatusAsFailed(Path path) {
        ObjectMapper mapper = jsonHelper.getMapper();
        JsonNode root = mapper.readTree(path.toFile());
        if (root instanceof ObjectNode objectNode) {
            objectNode.put("status", FAILED);
            String jsonString = mapper.writeValueAsString(objectNode);
            Files.writeString(path, jsonString, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

}
