package in.zeta.qa.utils.rest.ok_http;

import in.zeta.qa.utils.cuncurrency.SingletonFactory;
import in.zeta.qa.utils.misc.JsonHelper;
import in.zeta.qa.utils.rest.ApiRequest;
import in.zeta.qa.utils.rest.ApiResponse;
import in.zeta.qa.utils.rest.HttpClientService;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class OkHttpServiceImpl implements HttpClientService {

    public OkHttpServiceImpl() {};

    public static OkHttpServiceImpl getInstance() {
        return SingletonFactory.getInstance(OkHttpServiceImpl.class);
    }

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(60))
            .connectTimeout(Duration.ofSeconds(20))
            .readTimeout(Duration.ofSeconds(60))
            .writeTimeout(Duration.ofSeconds(60))
            .build();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType OCTET = MediaType.parse("application/octet-stream");


    @Override
    public ApiResponse execute(ApiRequest<?> request) {
        String baseUrl = buildUrl(request);
        HttpUrl url = addQueryParams(baseUrl, request.getQueryParams());
        Headers headers = request.getHeaders() != null
                ? Headers.of(request.getHeaders()) : new Headers.Builder().build();

        RequestBody requestBody = buildRequestBody(request);
        Request okRequest = new Request.Builder()
                .url(url)
                .headers(headers)
                .method(request.getMethod().name(), requestBody)
                .build();

        try (Response response = CLIENT.newCall(okRequest).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            Map<String, List<String>> respHeaders = response.headers().toMultimap();
            return ApiResponse.builder()
                    .statusCode(response.code())
                    .body(responseBody)
                    .headers(respHeaders)
                    .build();
        } catch (IOException e) {
            // Consider wrapping in runtime exception or returning error ApiResponse
            throw new RuntimeException("OkHttp call failed: " + e.getMessage(), e);
        }
    }

    private HttpUrl addQueryParams(String baseUrl, Map<String, Object> queryParams) {
        HttpUrl parsed = HttpUrl.parse(baseUrl);
        if (parsed == null) throw new IllegalArgumentException("Invalid URL: " + baseUrl);
        if (queryParams == null || queryParams.isEmpty()) return parsed;

        HttpUrl.Builder b = parsed.newBuilder();
        for (var e : queryParams.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();
            b.addQueryParameter(key, val == null ? null : String.valueOf(val));
        }
        return b.build();
    }

    private RequestBody buildRequestBody(ApiRequest<?> request) {
        if (request.getBody() == null) return null;

        // If multipart (files), build multipart
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            return buildMultipartBody(request.getBody(), request.getFormParams(), request.getFiles());
        }
        // If form params exist, build form-encoded
        if (request.getFormParams() != null && !request.getFormParams().isEmpty()) {
            FormBody.Builder fb = new FormBody.Builder(StandardCharsets.UTF_8);
            request.getFormParams().forEach((k, v) -> fb.add(k, v == null ? "" : String.valueOf(v)));
            return fb.build();
        }

        MediaType mt = contentTypeFromHeaders(request.getHeaders());
        if (request.getBody() instanceof String s) {
            return RequestBody.create(mt != null ? mt : JSON, s);
        }
        String json = new JsonHelper().convertObjectToJsonString(request.getBody());
        return RequestBody.create(mt != null ? mt : JSON, json);
    }

    private RequestBody buildMultipartBody(Object body, Map<String, Object> formParams, List<File> files) {
        MultipartBody.Builder mb = new MultipartBody.Builder().setType(MultipartBody.FORM);

        // Add form fields
        if (formParams != null) {
            formParams.forEach((k, v) -> mb.addFormDataPart(k, v == null ? "" : String.valueOf(v)));
        }

        // If body exists and is a Map, treat as extra fields (optional)
        if (body instanceof Map<?, ?> mapBody) {
            for (var e : mapBody.entrySet()) {
                if (e.getKey() != null) {
                    mb.addFormDataPart(String.valueOf(e.getKey()),
                            e.getValue() == null ? "" : String.valueOf(e.getValue()));
                }
            }
        }

        // Add files
        for (File f : files) {
            if (f == null) continue;
            RequestBody fileBody = RequestBody.create(OCTET, f);
            mb.addFormDataPart("file", f.getName(), fileBody); // field name "file" (customize if needed)
        }

        return mb.build();
    }

    private MediaType contentTypeFromHeaders(Map<String, String> headers) {
        if (headers == null) return null;
        String ct = null;
        for (var e : headers.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase("Content-Type")) {
                ct = e.getValue();
                break;
            }
        }
        return ct != null ? MediaType.parse(ct) : null;
    }

}
