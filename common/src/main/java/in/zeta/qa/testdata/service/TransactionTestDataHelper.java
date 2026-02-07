package in.zeta.qa.testdata.service;

import com.fasterxml.jackson.databind.JsonNode;
import in.zeta.qa.constants.CommonConstants;
import in.zeta.qa.constants.Networks;
import in.zeta.qa.constants.anotation.PostActionMapping;
import in.zeta.qa.testdata.entity.TransactionTestData;
import in.zeta.qa.testdata.entity.TxnPostActionData;
import in.zeta.qa.testdata.entity.user.Card;
import in.zeta.qa.utils.fileUtils.PropertyFileReader;
import in.zeta.qa.utils.misc.JsonHelper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class TransactionTestDataHelper {

    private static final Logger LOG = LogManager.getLogger(TransactionTestDataHelper.class);
    protected Networks network;
    protected List<Card> cards;

    protected TransactionTestDataHelper(Networks cardNetwork) {
        if (cardNetwork != null) {
            this.network = cardNetwork;
        }
    }

    /**
     * TEST DATA JSON RETRIEVAL
     */
    @SneakyThrows
    protected String getJsonDataForSheet(String fileName, String dataSheet) {
        String jsonData = Files.readString(new File(fileName).toPath(), StandardCharsets.UTF_8);
        JsonHelper jsonHelper = new JsonHelper();

        JsonNode root = jsonHelper.convertToJsonNode(jsonData);
        JsonNode sheets = root.get("data");

        for (int i = 0; i < sheets.size(); i++) {
            JsonNode sheet = sheets.get(i);
            if (sheet.get("sheetName").textValue().equalsIgnoreCase(dataSheet)) {
                return jsonHelper.convertJsonNodeToString(sheet.get("sheetData"));
            }
        }
        return null;
    }

    /**
     * GET ALL CARDS
     */
    @SneakyThrows
    public List<Card> getCards(String fileName) {
        String rubyProductId = PropertyFileReader.getPropertyValue("ruby.product.id");
        String cardSlotId = Objects.requireNonNullElse(PropertyFileReader.getPropertyValue("card.slot.id"), "1");

        this.cards = Objects.requireNonNullElseGet(this.cards, () ->
                getCardsWithoutProductFilter(fileName).stream()
                        .filter(c -> rubyProductId.equalsIgnoreCase(c.getProductId()))
                        .filter(s -> (StringUtils.isEmpty(s.getSlotId()) && "1".equals(cardSlotId))
                                || (StringUtils.isNotEmpty(s.getSlotId()) && cardSlotId.equalsIgnoreCase(s.getSlotId())))
                        .toList());
        return this.cards;
    }

    @SneakyThrows
    public List<Card> getCardsWithoutProductFilter(String fileName) {
        JsonHelper jsonHelper = new JsonHelper();
        String sheetJsonData = getJsonDataForSheet(fileName, CommonConstants.CARD_DATA);

        this.cards = jsonHelper.getObjectsFromString(sheetJsonData, Card.class).stream()
                .filter(c -> {
                    return PropertyFileReader.getPropertyValue("environment").toLowerCase().contains(c.getEnv().toLowerCase());
                })
                .peek(c -> c.setNetwork(this.network))
                .toList();

        return this.cards;
    }

    /**
     * FOR TEST CASES MAPPED with ONE ENTRY NO GROUP FILTER
     */
    @SneakyThrows
    private List<TransactionTestData> getTransactionDataWithoutFilter(String fileName, String dataSheet) {
        JsonHelper jsonHelper = new JsonHelper();
        String sheetJsonData = getJsonDataForSheet(fileName, dataSheet);

        LOG.info("Sheet name: {}", sheetJsonData);
        LOG.info("File name: {}", fileName);
        LOG.info("dataSheet name: {}", dataSheet);

        try {
            return jsonHelper.getObjectsFromString(sheetJsonData, TransactionTestData.class).stream()
                    .peek(s -> {
                        StringBuilder id = new StringBuilder();
                        if (Objects.nonNull(s.getTestGroup()) && !s.getTestGroup().isEmpty()) {
                            id.append(s.getTestGroup()).append("###");
                        }
                        if (Objects.nonNull(s.getBankTestCase()) && !s.getBankTestCase().isEmpty()) {
                            id.append(s.getBankTestCase()).append("###");
                        }
                        if (Objects.nonNull(s.getTachyonTestCaseId()) && !s.getTachyonTestCaseId().isEmpty()) {
                            id.append(s.getTachyonTestCaseId()).append("###");
                        }
                        s.setScenario(id + s.getScenario());
                    })
                    .toList();
        } catch (Exception e) {
            LOG.warn("Error parsing JSON: {}", e.getMessage());
        }
        return null;
    }

    /**
     * FOR TEST CASES MAPPED with ONE ENTRY
     */
    @SneakyThrows
    protected List<TransactionTestData> getTransactionData(String fileName, String dataSheet) {
        return Objects.requireNonNull(getTransactionDataWithoutFilter(fileName, dataSheet)).stream().toList();
    }

    /**
     * FOR TEST CASES MAPPED POST ACTIONS
     */
    @SneakyThrows
    protected <T extends TransactionTestData> List<T> getTransactionDataWithPostActions(String fileName, String dataSheet, Class<T> clazz) {
        JsonHelper jsonHelper = new JsonHelper();
        String sheetJsonData = getJsonDataForSheet(fileName, dataSheet);

        List<T> data = jsonHelper.getObjectsFromString(sheetJsonData, clazz);
        int cur = 0;
        boolean run = false;

        for (int i = 0; i < data.size(); i++) {
            if (Objects.nonNull(data.get(i).getRun()) && data.get(i).getRun()) {
                cur = i;
                run = true;
                data.get(cur).setPostActions(new ArrayList<>());
            } else if (data.get(i).getTachyonTestCaseId().isEmpty() && run) {
                if (Objects.isNull(data.get(cur).getPostActions())) {
                    data.get(cur).setPostActions(new ArrayList<>());
                }
                TxnPostActionData postAction = data.get(cur).getClass()
                        .getAnnotation(PostActionMapping.class).value()
                        .getDeclaredConstructor().newInstance();

                updatePostActionData(data.get(i), postAction);
                data.get(cur).getPostActions().add(postAction);
            } else if (Objects.nonNull(data.get(i).getRun()) && !data.get(i).getRun()) {
                run = false;
            }
        }

        return data.stream().filter(d -> Objects.nonNull(d.getRun()) && d.getRun())
                .filter(TestDataGrouping.filterByActiveTestAndTestGroup())
                .peek(s -> s.setScenario(getResultantTestScenario(s))).toList();
    }

    // subclasses to override
    protected <T extends TransactionTestData, U extends TxnPostActionData> void updatePostActionData(T data, U postAction) {
        updatePostActionWithCommonData(data, postAction);
    }

    protected void updatePostActionWithCommonData(TransactionTestData data, TxnPostActionData postAction) {
        postAction.setAction(data.getAction());
        Optional.ofNullable(data.getExpectedResponseCode())
                .filter(StringUtils::isNotEmpty)
                .ifPresent(postAction::setExpectedResponseCode);
        Optional.ofNullable(data.getExpectedErrorCode())
                .filter(StringUtils::isNotEmpty)
                .ifPresent(postAction::setExpectedErrorCode);
        postAction.setTxnAmount(data.getTxnAmount());
        Optional.ofNullable(data.getMccCode())
                .filter(StringUtils::isNotEmpty)
                .ifPresent(postAction::setMcc);
    }

    @SneakyThrows
    protected List<List<TransactionTestData>> getAuthTransactionDataWithMultipleTxnTypeAction(String fileName, String dataSheet) {
        JsonHelper jsonHelper = new JsonHelper();
        String sheetJsonData = getJsonDataForSheet(fileName, dataSheet);

        List<TransactionTestData> data = jsonHelper.getObjectsFromString(sheetJsonData, TransactionTestData.class);
        List<List<TransactionTestData>> result = new ArrayList<>();
        List<TransactionTestData> currentGroup = new ArrayList<>();

        for (TransactionTestData entry : data) {
            if (BooleanUtils.isTrue(entry.getRun())) {
                if (!currentGroup.isEmpty()) {
                    result.add(new ArrayList<>(currentGroup));
                    currentGroup.clear();
                }
                currentGroup.add(entry);
            } else if (entry.getRun() == null) {
                if (!currentGroup.isEmpty()) {
                    currentGroup.add(entry);
                    result.add(new ArrayList<>(currentGroup));
                    currentGroup.clear();
                }
            }
        }

        if (!currentGroup.isEmpty()) {
            result.add(currentGroup);
        }

        return result.stream().filter(d -> Objects.nonNull(d.get(0).getRun()) && d.get(0).getRun())
                .peek(group -> group.get(0).setScenario(getResultantTestScenario(group.get(0))))
                .toList();
    }

    private String getResultantTestScenario(TransactionTestData s) {
        StringBuilder id = new StringBuilder();
        if (Objects.nonNull(s.getTestGroup()) && !s.getTestGroup().isEmpty()) {
            id.append(s.getTestGroup()).append("###");
        }
        if (Objects.nonNull(s.getBankTestCase()) && !s.getBankTestCase().isEmpty()) {
            id.append(s.getBankTestCase()).append("###");
        }
        if (Objects.nonNull(s.getTachyonTestCaseId()) && !s.getTachyonTestCaseId().isEmpty()) {
            id.append(s.getTachyonTestCaseId()).append("###");
        }
        if (Objects.nonNull(s.getScenario()) && !s.getScenario().isEmpty()) {
            id.append(s.getScenario());
        }
        return id.toString();
    }

}
