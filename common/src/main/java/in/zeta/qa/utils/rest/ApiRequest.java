package in.zeta.qa.utils.rest;


import in.zeta.qa.constants.endpoints.ApiEndpoint;
import in.zeta.qa.utils.misc.JsonHelper;
import in.zeta.qa.utils.misc.RetryUtils;
import lombok.Builder;
import lombok.Value;

import java.io.File;
import java.util.List;
import java.util.Map;

@Builder(builderClassName = "ApiRequestBuilder", toBuilder = true)
@Value
public class ApiRequest<T> {

    ClientType client = ClientType.REST_ASSURED;   // 👈 NEW FIELD

    //URL and Path
    // Full URL (if you want to override serverURL + endpoint + pathParams)
    String serverURL; // BASE url OR HOST
    ApiEndpoint endpoint; // Endpoint with path template
    Map<String, Object> pathParams; // Path parameters to replace in endpoint

    // HTTP Method & Headers
    HttpMethod method;
    Map<String, String> headers;
    // Query parameters
    Map<String, Object> queryParams;
    // Body can be String (JSON, XML), POJO, Map etc.
    T body;
    Map<String, Object> formParams;
    // For Basic Auth
    String username;
    String password;
    // For File Uploads
    List<File> files;

    public static class ApiRequestBuilder<T> {
        public ApiResponse execute() {
            return this.build().execute();
        }

        public <R> R execute(Class<R> responseClass) {
            return this.build().execute(responseClass);
        }
    }


    public ApiResponse execute() {
        return new RetryUtils().executeWithRetry(() -> client.getService().execute(this));
    }

    public <R> R execute(Class<R> responseClass) {
        ApiResponse response = execute();
        return new JsonHelper().getObjectFromString(response.getBody(), responseClass);
    }

}
