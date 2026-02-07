package in.zeta.qa.utils.rest;

import in.zeta.qa.utils.misc.AssertHelper;
import in.zeta.qa.utils.misc.JsonHelper;
import io.restassured.response.Response;

import java.util.Optional;

public class ResponseParserUtil {

    private ResponseParserUtil() {} // prevent instantiation

    public static <T> Optional<T> parseResponse(Response response,
            int expectedStatusCode, Class<T> clazz,
            AssertHelper assertHelper, JsonHelper jsonHelper) {
        try {
            assertHelper.validateStatusCode(response, expectedStatusCode);
            T result = jsonHelper.getObjectFromString(response.body().asString(), clazz);
            return Optional.of(result);
        } catch (AssertionError e) {
            return Optional.empty();
        }
    }
}
