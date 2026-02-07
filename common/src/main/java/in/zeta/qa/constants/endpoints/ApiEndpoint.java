package in.zeta.qa.constants.endpoints;

import java.util.Map;

public interface ApiEndpoint {
    String getPath();

    default String build(String baseUrl, Object... params) {
        return baseUrl + String.format(getPath(), params);
    }

    default String build(String baseUrl, Map<String, Object> params) {
        String resolved = getPath();
        for (var entry : params.entrySet()) {
            resolved = resolved.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return baseUrl + resolved;
    }
}
