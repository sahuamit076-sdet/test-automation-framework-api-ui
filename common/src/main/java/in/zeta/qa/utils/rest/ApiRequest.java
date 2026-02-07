package in.zeta.qa.utils.rest;


import in.zeta.qa.constants.endpoints.ApiEndpoint;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import io.restassured.response.Response;
import lombok.Builder;
import lombok.Value;

import java.io.File;
import java.util.List;
import java.util.Map;

@Builder(builderClassName = "ApiRequestBuilder", toBuilder = true)
@Value
public class ApiRequest<T> {
    //URL and Path
    // Full URL (if you want to override serverURL + endpoint + pathParams)
    String serverURL; // BASE url OR HOST
    ApiEndpoint endpoint; // Endpoint with path template
    Map<String, Object> pathParams; // Path parameters to replace in endpoint

    // HTTP Method & Headers
    Method method;
    Headers headers;
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
        public Response execute() {
            return this.build().execute();
        }

        public <R> R execute(Class<R> responseClass) {
            return this.build().execute(responseClass);
        }
    }


    public Response execute() {
        return RestFactory.getClient().execute(this);
    }

    public <R> R execute(Class<R> responseClass) {
        Response response = execute();
        return response.as(responseClass);
    }

}
