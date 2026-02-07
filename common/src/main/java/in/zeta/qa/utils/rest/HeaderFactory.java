package in.zeta.qa.utils.rest;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.restassured.http.Header;
import io.restassured.http.Headers;

import java.util.concurrent.TimeUnit;

public class HeaderFactory {

    // Cache for single Header objects
    private static final Cache<String, Header> headerCache = Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .build();

    // Cache for combined Headers objects
    private static final Cache<String, Headers> headersCache = Caffeine.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .build();
    // ----------------------
    // Private helpers
    // ----------------------

    private static Header getOrCreateHeader(String key, String value) {
        String cacheKey = key + "::" + value;
        return headerCache.get(cacheKey, k -> new Header(key, value));
    }

    private static Headers getOrCreateHeaders(String cacheKey, Header... headers) {
        return headersCache.get(cacheKey, k -> new Headers(headers));
    }
    // Static headers (private)
    public static Header contentTypeJson() {
        return getOrCreateHeader("Content-Type", "application/json");
    }

    private static Header acceptJson() {
        return getOrCreateHeader("Accept", "application/json");
    }

    private static Header contentTypeXml() {
        return getOrCreateHeader("Content-Type", "application/xml");
    }

    private static Header contentTypeFormBody() {
        return getOrCreateHeader("Content-Type", "application/x-www-form-urlencoded");
    }

    // Dynamic headers (private)
    private static Header userAgent(String agent) {
        return getOrCreateHeader("User-Agent", agent);
    }

    public static Header authorization(String token) {
        return getOrCreateHeader("Authorization", "Bearer " + token);
    }

    private static Header zetaApiToken(String token) {
        return getOrCreateHeader("X-Zeta-AuthToken", token);
    }

    private static Header apiKey(String apiKey) {
        return getOrCreateHeader("apiKey", apiKey);
    }

    private static Header jwt(String token) {
        return getOrCreateHeader("X-Jwt-AuthToken", token);
    }

    // ----------------------
    // Public methods (always return Headers)
    // ----------------------
    public static Headers authorizationWithJsonContentType(String token) {
        String cacheKey = "authorizationWithJsonContentType::" + token;
        return getOrCreateHeaders(cacheKey, authorization(token), contentTypeJson());
    }

    public static Headers zetaApiTokenWithJsonContentType(String token) {
        String cacheKey = "zetaApiTokenWithJsonContentType::" + token;
        return getOrCreateHeaders(cacheKey, zetaApiToken(token), contentTypeJson());
    }

    public static Headers jwtWithJsonContentType(String token) {
        String cacheKey = "jwtWithJsonContentType::" + token;
        return getOrCreateHeaders(cacheKey, jwt(token), contentTypeJson());
    }

    public static Headers userAgentWithJsonContentType(String agent) {
        String cacheKey = "userAgentWithJsonContentType::" + agent;
        return getOrCreateHeaders(cacheKey, userAgent(agent), contentTypeJson());
    }

    public static Headers basicAuthWithFormUrlEncoded(String encoded) {
        String cacheKey = "basicAuthWithFormUrlEncoded::" + encoded;
        return getOrCreateHeaders(cacheKey, getOrCreateHeader("Authorization", "Basic " + encoded), contentTypeFormBody());
    }

    public static Headers contentTypeJsonHeader() {
        String cacheKey = "contentTypeJson";
        return getOrCreateHeaders(cacheKey, contentTypeJson());
    }

    public static Headers contentTypeXmlHeader() {
        String cacheKey = "contentTypeXml";
        return getOrCreateHeaders(cacheKey, contentTypeXml());
    }

    public static Headers zetaApiTokenHeader(String token) {
        String cacheKey = "zetaApi::" + token;
        return getOrCreateHeaders(cacheKey, zetaApiToken(token));
    }

    public static Headers authorizationHeader(String token) {
        String cacheKey = "authorization::" + token;
        return getOrCreateHeaders(cacheKey, authorization(token));
    }

    public static Headers apikeyTokenHeader(String token) {
        String cacheKey = "apiKey::" + token;
        return getOrCreateHeaders(cacheKey, apiKey(token));
    }
}
