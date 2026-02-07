package in.zeta.qa.utils.rest;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.zeta.qa.constants.endpoints.ApiEndpoint;
import in.zeta.qa.utils.fileUtils.XmlUtils;
import in.zeta.qa.utils.misc.JsonHelper;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;


@Slf4j
class RestClientServiceImpl implements HttpClientService {

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
    // URL
    // ---------------------

    private String buildUrl(ApiRequest<?> restRequest) {
        String resolved = Optional.ofNullable(restRequest.getEndpoint())
                .map(ApiEndpoint::getPath)
                .orElse("");

        if (Objects.nonNull(restRequest.getPathParams())) {
            for (var entry : restRequest.getPathParams().entrySet()) {
                resolved = resolved.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }

        return restRequest.getServerURL() + resolved;
    }


    // ---------------------
    // Headers
    // ---------------------
    private void applyHeaders(RequestSpecification request, ApiRequest<?> restRequest) {
        Optional.ofNullable(restRequest.getHeaders())
                .ifPresent(request::headers);
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
    // Query parameters
    // ---------------------
    private void applyQueryParams(RequestSpecification request, ApiRequest<?> restRequest) {
        Optional.ofNullable(restRequest.getQueryParams())
                .ifPresent(request::queryParams);
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
        return Optional.ofNullable(restRequest.getHeaders())
                .map(headers -> headers.getValue("Content-Type"))
                .map(String::toLowerCase).filter(header -> header.contains("xml")).map(header -> ContentType.XML).orElse(ContentType.JSON);
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

    //============================================================================================
    // Convenience methods for common HTTP methods

    @Override
    public Response execute(ApiRequest<?> restRequest) {
        String url = buildUrl(restRequest);

        RequestSpecification request = RestAssured.given()
                .config(CONFIG)
                .relaxedHTTPSValidation().filter(new CurlLoggingFilter())
                .filter(new AllureRestAssured().setRequestAttachmentName(url));

        applyHeaders(request, restRequest);
        applyAuthentication(request, restRequest);
        applyQueryParams(request, restRequest);
        applyFormParams(request, restRequest);
        applyBody(request, restRequest);
        applyFiles(request, restRequest);

        try {
            return executeRequest(request, url, restRequest.getMethod());
        } catch (Exception e) {
            if (containsNoHttpResponseException(e)) {
                try {
                    return executeRequest(request, url, restRequest.getMethod());
                } catch (Exception ex) {
                    log.error("Error executing request to URL: {}", url, ex);
                }
            }
        }

        return null;
    }

    private boolean containsNoHttpResponseException(Throwable t) {
        while (t != null) {
            if (t instanceof org.apache.http.NoHttpResponseException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }


}
