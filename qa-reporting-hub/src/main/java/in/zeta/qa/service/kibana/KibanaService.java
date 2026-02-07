package in.zeta.qa.service.kibana;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.zeta.qa.constants.FilePath;
import in.zeta.qa.entity.kibana.KibanaResponse;
import in.zeta.qa.utils.fileUtils.PropertyFileReader;
import in.zeta.qa.utils.misc.JsonHelper;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class KibanaService implements FilePath {

    private final KibanaClient kibanaClient = new KibanaClient();
    private final JsonHelper jsonHelper = new JsonHelper();

    @SneakyThrows
    public String getKibanaUrlForErrorLogs(List<String> clusters, long fromEpochTime, long toEpochTime) {
        String longUrlPath = buildKibanaUrlForErrorLogs(clusters, fromEpochTime, toEpochTime);
        Response response = kibanaClient.shortUrl(longUrlPath);
        String server = PropertyFileReader.getPropertyValue("kibana.server.url");
        if (response.statusCode() == HttpStatus.SC_OK) {
            JsonNode jsonNode = jsonHelper.convertToJsonNode(response.body().asString());
            String uuid = jsonNode.get("urlId").textValue();
            return server + "/_dashboards/goto/" + uuid + "?security_tenant=global";
        }
        return null;
    }

    public String buildKibanaUrlForErrorLogs(List<String> clusterNames, long startMillis, long endMillis) {
        String timeFrom = Instant.ofEpochMilli(startMillis).toString(); // ISO 8601 format
        String timeTo = Instant.ofEpochMilli(endMillis).toString();

        // Create match_phrase clauses for filters
        String matchClauses = clusterNames.stream()
                .map(name -> "(match_phrase:(clusterName:" + name + "))")
                .collect(Collectors.joining(","));

        // For params and value field (no spaces)
        String clusterParams = clusterNames.stream().collect(Collectors.joining(","));
        return "/app/discover#/?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:'"
                + timeFrom + "',to:'" + timeTo + "'))"
                + "&_a=(columns:!(clusterName,parsedMessage.title,mdc.trace_id,parsedMessage.level),"
                + "filters:!((meta:(index:'logs-*',key:clusterName,params:!(" + clusterParams + "),type:phrases),"
                + "query:(bool:(minimum_should_match:1,should:!(" + matchClauses + ")))),"
                + "(exists:(field:parsedMessage.title)),"
                + "(meta:(index:'logs-*',key:parsedMessage.level,params:(query:ERROR),type:phrase),"
                + "query:(match_phrase:(parsedMessage.level:ERROR))))"
                + ",index:'logs-*',interval:auto,query:(language:kuery,query:''),sort:!())";
    }

    //##################################################################################################################
    //######################################## KIBANA OPEN SEARCH ######################################################
    //##################################################################################################################

    @SneakyThrows
    private String constructPayloadForErrorLogs(List<String> apps, long fromEpochTime, long toEpochTime) {
        String payload = FileUtils.readFileToString(new File(KIBANA_SEARCH_PAYLOAD), "UTF-8");
        JsonNode root = jsonHelper.convertToJsonNode(payload);
        //APPs
        ObjectNode boolNode = (ObjectNode) root.at("/params/body/query/bool/filter/1/bool");
        ArrayNode shouldArray = boolNode.putArray("should");
        // Clear existing entries
        if (shouldArray != null) {
            shouldArray.removeAll();
        } else {
            shouldArray = boolNode.putArray("should");
        }
        for (String app : apps) {
            ObjectNode matchPhrase = shouldArray.addObject();
            ObjectNode appNode = matchPhrase.putObject("match_phrase");
            appNode.put("app", app);
        }
        boolNode.put("minimum_should_match", 1);

        //TIME RANGE
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC); // Convert to IST time zone
        String fromTime = formatter.format(Instant.ofEpochMilli(fromEpochTime));
        String toTime = formatter.format(Instant.ofEpochMilli(toEpochTime));

        ObjectNode rangeNode = (ObjectNode) root.at("/params/body/query/bool/filter/3/range/timestamp");
        rangeNode.put("gte", fromTime);
        rangeNode.put("lte", toTime);
        rangeNode.put("format", "strict_date_optional_time");

        return jsonHelper.convertJsonNodeToString(rangeNode);
    }

    @SneakyThrows
    private String constructPayloadUsingMatchPhrase(long fromEpochTime, long toEpochTime, Map<String, String> matchKeyValue) {
        String payload = FileUtils.readFileToString(new File(KIBANA_SEARCH_PAYLOAD), StandardCharsets.UTF_8);
        JsonNode root = jsonHelper.convertToJsonNode(payload);
        ArrayNode filterNode = (ArrayNode) root.at("/params/body/query/bool/filter");
        // Add match_phrase filters to query for
        matchKeyValue.forEach((key, value) -> {
            ObjectNode matchPhrase = jsonHelper.getMapper().createObjectNode();
            matchPhrase.set("match_phrase", jsonHelper.getMapper().createObjectNode().put(key, value));
            filterNode.add(matchPhrase);
        });
        // Add time range filter, convert the given epoch(IST) time to UTC time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(ZoneId.of("UTC"));
        ObjectNode timestampNode = jsonHelper.getMapper().createObjectNode();
        timestampNode.put("gte", formatter.format(Instant.ofEpochMilli(fromEpochTime)));
        timestampNode.put("lte", formatter.format(Instant.ofEpochMilli(toEpochTime)));
        timestampNode.put("format", "strict_date_optional_time");
        ObjectNode rangeNode = jsonHelper.getMapper().createObjectNode();
        rangeNode.set("timestamp", timestampNode);
        ObjectNode rangeWrapper = jsonHelper.getMapper().createObjectNode();
        rangeWrapper.set("range", rangeNode);
        filterNode.add(rangeWrapper);
        return jsonHelper.convertJsonNodeToString(root);
    }

    //TODO(amit): use this for fetching stacktrace of error
    public void searchErrorLogs(List<String> apps, long fromEpochTime, long toEpochTime) {
        String payload = constructPayloadForErrorLogs(apps, fromEpochTime, toEpochTime);
        Response response = kibanaClient.openSearch(payload);
    }

    public Predicate<KibanaResponse.HitItem> getMatchingRrnAndTitlePredicate(String rrn, String title) {
        return hit -> Optional.ofNullable(hit.get_source())
                .map(KibanaResponse.Source::getParsedMessage)
                .filter(parsedMessage -> Optional.ofNullable(parsedMessage.getAttributes())
                        .map(KibanaResponse.Attributes::getRrn)
                        .map(r -> r.equalsIgnoreCase(rrn)).orElse(false) &&
                        Optional.ofNullable(parsedMessage.getTitle())
                                .map(t -> t.equalsIgnoreCase(title)).orElse(false)).isPresent();
    }

    public Predicate<KibanaResponse.HitItem> getMatchingRrnPredicate(String rrn) {
        return hit -> Optional.ofNullable(hit.get_source())
                .map(KibanaResponse.Source::getParsedMessage)
                .filter(parsedMessage -> Optional.ofNullable(parsedMessage.getAttributes())
                        .map(KibanaResponse.Attributes::getRrn)
                        .map(r -> r.equalsIgnoreCase(rrn)).orElse(false)).isPresent();
    }

    public List<KibanaResponse.HitItem> getLogMessages(KibanaResponse kibanaResponse) {
        return Optional.ofNullable(kibanaResponse)
                .map(KibanaResponse::getRawResponse)
                .map(KibanaResponse.RawResponse::getHits)
                .map(KibanaResponse.Hits::getHitList)
                .orElse(Collections.emptyList());
    }

    /**
     * Returns all matching hit items based on given RRN.
     */
    public List<KibanaResponse.HitItem> searchMatchingHitsByRrn(long fromEpochTime, long toEpochTime, String rrn,
                                                                Map<String, String> matchPhraseMap) {
        String payload = constructPayloadUsingMatchPhrase(fromEpochTime, toEpochTime, matchPhraseMap);
        Response response = kibanaClient.openSearch(payload);
        KibanaResponse kibanaResponse = jsonHelper.getObjectFromString(response.body().asString(), KibanaResponse.class);
        Predicate<KibanaResponse.HitItem> predicate = getMatchingRrnPredicate(rrn);
        return getLogMessages(kibanaResponse).stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Returns all matching hit items based on given RRN and title on given matchPhrase
     */
    public List<KibanaResponse.HitItem> searchMatchingHitsByRrnAndTitle(long fromEpochTime, long toEpochTime, String rrn,
                                                                        String title, Map<String, String> matchPhraseMap) {
        String payload = constructPayloadUsingMatchPhrase(fromEpochTime, toEpochTime, matchPhraseMap);
        Response response = kibanaClient.openSearch(payload);
        KibanaResponse kibanaResponse = jsonHelper.getObjectFromString(response.body().asString(), KibanaResponse.class);
        Predicate<KibanaResponse.HitItem> predicate = getMatchingRrnAndTitlePredicate(rrn, title);
        return getLogMessages(kibanaResponse).stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Returns all matching log messages based on given matchPhrase by user.
     */
    public List<KibanaResponse.HitItem> searchLogsWithMatchPhrase(long fromEpochTime, long toEpochTime, Map<String, String> matchPhraseMap) {
        String payload = constructPayloadUsingMatchPhrase(fromEpochTime, toEpochTime, matchPhraseMap);
        Response response = kibanaClient.openSearch(payload);
        KibanaResponse kibanaResponse = jsonHelper.getObjectFromString(response.body().asString(), KibanaResponse.class);
        return getLogMessages(kibanaResponse);
    }

    /**
     * Returns all log message based on given matchPhrase by user, with matchStr present
     */
    public List<KibanaResponse.HitItem> searchLogsWithMatchPhraseUntilMatchingStrFound(long fromEpochTime, long toEpochTime,
                                                                                       String matchStr, Map<String, String> matchPhraseMap) {
        String payload = constructPayloadUsingMatchPhrase(fromEpochTime, toEpochTime, matchPhraseMap);
        Response response = kibanaClient.openSearchWithRetryUntilMatchedStrFound(payload, matchStr);
        KibanaResponse kibanaResponse = jsonHelper.getObjectFromString(response.body().asString(), KibanaResponse.class);
        return getLogMessages(kibanaResponse);
    }
}
