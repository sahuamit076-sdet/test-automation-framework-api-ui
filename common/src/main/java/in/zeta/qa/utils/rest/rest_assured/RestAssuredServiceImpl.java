package in.zeta.qa.utils.rest.rest_assured;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.zeta.qa.utils.fileUtils.XmlUtils;
import in.zeta.qa.utils.misc.JsonHelper;
import in.zeta.qa.utils.rest.ApiRequest;
import in.zeta.qa.utils.rest.ApiResponse;
import in.zeta.qa.utils.rest.HttpClientService;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.NoHttpResponseException;


@Slf4j
public class RestAssuredServiceImpl implements HttpClientService {

    JsonHelper jsonHelper = new JsonHelper();
    XmlUtils xmlUtils = new XmlUtils();

    private static final RestAssuredConfig CONFIG;

    static {
        ObjectMapper customMapper = new JsonHelper().getMapper();
        CONFIG = RestAssuredConfig.config()
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                        .jackson2ObjectMapperFactory((cls, charset) -> customMapper)
                );
    }


    // ---------------------
    // EXECUTION
    // ---------------------

    private Response executeRequest(RequestSpecification request, String serverURL, Method method) {
        return switch (method) {
            case GET -> request.get(serverURL);
            case POST -> request.post(serverURL);
            case PUT -> request.put(serverURL);
            case PATCH -> request.patch(serverURL);
            case DELETE -> request.delete(serverURL);
            case HEAD, OPTIONS, TRACE -> null;
        };
    }


    // ---------------------
    // Authentication
    // ---------------------
    private void applyAuthentication(RequestSpecification request, ApiRequest<?> restRequest) {
        if (restRequest.getUsername() != null && restRequest.getPassword() != null) {
            request.auth().preemptive().basic(restRequest.getUsername(), restRequest.getPassword());
        }
    }


    // ---------------------
    // Form parameters
    // ---------------------
    private void applyFormParams(RequestSpecification request, ApiRequest<?> restRequest) {
        Optional.ofNullable(restRequest.getFormParams()).ifPresent(formParams -> {
            request.formParams(formParams);
            request.contentType(ContentType.URLENC);
        });
    }

    // ---------------------
    // Body (POJO → JSON/XML or String)
    // ---------------------
    private void applyBody(RequestSpecification request, ApiRequest<?> restRequest) {
        Optional.ofNullable(restRequest.getBody()).ifPresent(body -> {
            ContentType bodyContentType = determineContentType(restRequest);
            request.contentType(bodyContentType);

            try {
                if (body instanceof String strBody) {
                    request.body(strBody);
                } else if (body instanceof Map<?, ?> mapBody) {
                    @SuppressWarnings("unchecked")
                    String payload = jsonHelper.convertMapToJsonString((Map<String, Object>) mapBody);
                    request.body(payload);
                } else if (body instanceof JsonNode nodeBody) {
                    String payload = jsonHelper.convertJsonNodeToString(nodeBody);
                    request.body(payload);
                } else {
                    String serializedBody = (bodyContentType == ContentType.XML)
                            ? xmlUtils.convertObjectToXmlString(body)
                            : jsonHelper.convertObjectToJsonString(body);
                    request.body(serializedBody);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize request body", e);
            }
        });
    }

    private ContentType determineContentType(ApiRequest<?> restRequest) {
        if (restRequest.getHeaders() != null) {
            String contentType = restRequest.getHeaders().get("Content-Type");
            if (contentType != null && contentType.toLowerCase().contains("xml")) {
                return ContentType.XML;
            }
        }
        return ContentType.JSON; // default
    }

    // ---------------------
    // Files (multipart)
    // ---------------------
    private void applyFiles(RequestSpecification request, ApiRequest<?> restRequest) {
        Optional.ofNullable(restRequest.getFiles())
                .filter(files -> !files.isEmpty())
                .ifPresent(files -> {
                    request.contentType(ContentType.MULTIPART);
                    files.forEach(file -> request.multiPart("file", file));
                });
    }

    private ApiResponse toApiResponse(Response response) {
        return ApiResponse.builder()
                .statusCode(response.getStatusCode())
                .headers(response.getHeaders().asList().stream()
                        .collect(Collectors.groupingBy(
                                Header::getName, Collectors.mapping(Header::getValue, Collectors.toList())
                        )))
                .body(response.getBody().asString())
                .build();
    }

    //============================================================================================

    @Override
    public ApiResponse execute(ApiRequest<?> restRequest) {
        String url = buildUrl(restRequest);

        RequestSpecification request = RestAssured.given()
                .config(CONFIG)
                .relaxedHTTPSValidation().filter(new CurlLoggingFilter())
                .filter(new AllureRestAssured().setRequestAttachmentName(url));

        Optional.ofNullable(restRequest.getHeaders()).ifPresent(request::headers);
        Optional.ofNullable(restRequest.getQueryParams()).ifPresent(request::queryParams);

        applyAuthentication(request, restRequest);
        applyFormParams(request, restRequest);
        applyBody(request, restRequest);
        applyFiles(request, restRequest);

        Response response = null;
        try {
            response = executeRequest(request, url, Method.valueOf(restRequest.getMethod().name()));
        } catch (Exception e) {
            if (containsNoHttpResponseException(e)) {
                try {
                    response = executeRequest(request, url, Method.valueOf(restRequest.getMethod().name()));
                } catch (Exception ex) {
                    log.error("Error executing request to URL: {}", url, ex);
                }
            }
        }

        return toApiResponse(response);
    }

    private boolean containsNoHttpResponseException(Throwable t) {
        while (t != null) {
            if (t instanceof NoHttpResponseException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }


}
