package in.zeta.qa.utils.rest;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HeaderFactory {

    private HeaderFactory() {} // prevent instantiation

    // ----------------------
    // Caches
    // ----------------------
    private static final Cache<String, Map<String, String>> headersCache = Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .build();

    private static final Cache<String, Map<String, String>> singleHeaderCache = Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .build();

    // ----------------------
    // Private helper methods
    // ----------------------
    private static Map<String, String> getOrCreateHeader(String key, String value) {
        String cacheKey = key + "::" + value;
        return singleHeaderCache.get(cacheKey, k -> Map.of(key, value));
    }

    @SafeVarargs
    private static Map<String, String> getOrCreateHeaders(String cacheKey, Map<String, String>... headers) {
        return headersCache.get(cacheKey, k -> {
            Map<String, String> combined = new HashMap<>();
            for (Map<String, String> h : headers) {
                combined.putAll(h);
            }
            return Collections.unmodifiableMap(combined);
        });
    }


    // ----------------------
    // Static headers
    // ----------------------
    public static Map<String, String> contentTypeJson() {
        return getOrCreateHeader("Content-Type", "application/json");
    }

    public static Map<String, String> acceptJson() {
        return getOrCreateHeader("Accept", "application/json");
    }

    public static Map<String, String> contentTypeXml() {
        return getOrCreateHeader("Content-Type", "application/xml");
    }

    public static Map<String, String> contentTypeFormBody() {
        return getOrCreateHeader("Content-Type", "application/x-www-form-urlencoded");
    }

    // ----------------------
    // Dynamic headers
    // ----------------------
    public static Map<String, String> authorization(String token) {
        return getOrCreateHeader("Authorization", "Bearer " + token);
    }

    public static Map<String, String> zetaApiToken(String token) {
        return getOrCreateHeader("X-Zeta-AuthToken", token);
    }

    public static Map<String, String> jwt(String token) {
        return getOrCreateHeader("X-Jwt-AuthToken", token);
    }

    public static Map<String, String> userAgent(String agent) {
        return getOrCreateHeader("User-Agent", agent);
    }

    public static Map<String, String> apiKey(String apiKey) {
        return getOrCreateHeader("apiKey", apiKey);
    }

    // ----------------------
    // Combined headers
    // ----------------------
    public static Map<String, String> authorizationWithJson(String token) {
        return getOrCreateHeaders("authorizationWithJson::" + token,
                authorization(token),
                contentTypeJson());
    }

    public static Map<String, String> zetaApiTokenWithJson(String token) {
        return getOrCreateHeaders("zetaApiTokenWithJson::" + token,
                zetaApiToken(token),
                contentTypeJson());
    }

    public static Map<String, String> jwtWithJson(String token) {
        return getOrCreateHeaders("jwtWithJson::" + token,
                jwt(token),
                contentTypeJson());
    }

    public static Map<String, String> userAgentWithJson(String agent) {
        return getOrCreateHeaders("userAgentWithJson::" + agent,
                userAgent(agent),
                contentTypeJson());
    }

    public static Map<String, String> basicAuthWithFormBody(String encoded) {
        Map<String, String> authHeader = getOrCreateHeader("Authorization", "Basic " + encoded);
        return getOrCreateHeaders("basicAuthWithFormBody::" + encoded,
                authHeader,
                contentTypeFormBody());
    }
}
