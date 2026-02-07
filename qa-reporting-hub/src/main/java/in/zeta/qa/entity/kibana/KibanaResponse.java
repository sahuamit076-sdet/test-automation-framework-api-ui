package in.zeta.qa.entity.kibana;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KibanaResponse {
    private boolean isPartial;
    private boolean isRunning;
    private RawResponse rawResponse;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawResponse {
        private int took;
        private boolean timed_out;
        private Shards _shards;
        private Hits hits;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Shards {
        private int total;
        private int successful;
        private int skipped;
        private int failed;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hits {
        private int total;
        private Object max_score;
        @JsonAlias("hits")
        private List<HitItem> hitList;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HitItem {
        private String _index;
        private String _type;
        private String _id;
        private int _version;
        private Object _score;
        private Source _source;
        private Map<String, List<String>> fields;
        private Map<String, List<String>> highlight;
        private List<Long> sort;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {
        private String timestamp;
        private String app;
        private String clusterName;
        private String host;
        private String logger;
        private String threadName;
        private String node;
        private String pod;
        private String container;
        private String deployment;
        private String service;
        private String level;
        private Mdc mdc;
        private String zoneName;
        private ParsedMessage parsedMessage;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Mdc {
        private String trace_id;
        private String pod;
        private String trace_flags;
        private String span_id;
        private String flowID;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedMessage {
        private String flowId;
        private String id;
        private String type;
        private ParsedMessageTime time;
        private String title;
        private String level;
        private Attributes attributes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedMessageTime {
        private String sec;
        private String micros;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attributes {
        private String mti;
        private String maskedPan;
        private String connectionCode;
        private String bin;
        private String isNetworkMessage;
        private String byteSent;
        private String network;
        private String responseTimeInMS;
        private String messageIdentifier;
        private String localAddress;
        private String stan;
        private String context;
        private String tenantID;
        private String mid;
        private String actionCode;
        private String acquiringInstIdCode;
        private String cpsData;
        private String tid;
        private String rrn;
        private String flowId;
        private String nonsensitiveFields;
        private String remoteAddress;
        private String reportOutputChannelId;
        private String reportId;
        private String reportName;
        private String reportScheduleName;
        private String outputSettings;
        private String reportScheduleId;
        private String generatedReportId;
        private String subject;
        private String toEmails;
        private String ccEmails;
        private String message;
    }
}
