package in.zeta.qa.utils.jenkins;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

public class CurlBuilder {

    private static StringBuilder generateCurlWithoutBody(Method method, Headers customHeaders) {
        StringBuilder curl = new StringBuilder();
        curl.append("curl --location");
        // Add method
        curl.append(" --request ").append(method.name());
        // Add headers
        for (Header header : customHeaders) {
            curl.append(" --header '").append(header.getName()).append(": ").append(header.getValue()).append("'");
        }
        return curl;
    }

    private static String addUri(StringBuilder curl, String url) {
        // Add URL
        curl.append(" '").append(url).append("'");
        return curl.toString();
    }

    public static String generateCurlCommand(String url, Method method, Headers customHeaders, String payload) {
        StringBuilder curl = generateCurlWithoutBody(method, customHeaders);
        // Add payload if present
        if (StringUtils.isNotEmpty(payload)) {
            // Escape any single quotes in the payload
            String escapedPayload = payload.replace("'", "'\\''");
            curl.append(" --data '").append(escapedPayload).append("'");
        }
        // Add URL
        return addUri(curl, url);
    }

    public static String generateCurlCommandWithFormBody(String url, Method method, Headers customHeaders, Map<String, Object> formPayload) {
        StringBuilder curl = generateCurlWithoutBody(method, customHeaders);
        // Add form-encoded data if provided
        if (formPayload != null && !formPayload.isEmpty()) {
            String encodedParams = formPayload.entrySet().stream()
                    .map(entry -> " --data-urlencode '" + entry.getKey() + "=" + entry.getValue() + "'")
                    .collect(Collectors.joining());
            curl.append(encodedParams);
        }
        // Add URL
        return addUri(curl, url);
    }
}
