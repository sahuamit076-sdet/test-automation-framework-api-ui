package in.zeta.qa.utils.misc;

import in.zeta.qa.constants.ErrorConstants;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.*;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;


@Slf4j
public class AssertHelper implements ErrorConstants {
    private final JsonHelper jsonHelper = new JsonHelper();



    /* ----------------------- HTTP Assertions ----------------------- */

    @AllArgsConstructor
    @Getter
    public enum HttpStatusRange {
        INFORMATIONAL(100, 199),
        SUCCESS(200, 299),
        REDIRECTION(300, 399),
        CLIENT_ERROR(400, 499),
        SERVER_ERROR(500, 599);

        private final int min;
        private final int max;

        public boolean contains(int status) {
            return status >= min && status <= max;
        }
    }


    // Convenience method for 2xx
    public static void is2xx(Response response) {
        assertStatusInRange(response, HttpStatusRange.SUCCESS, true);
    }

    // Convenience method for 4xx
    public static void not4xx(Response response) {
        assertStatusInRange(response, HttpStatusRange.CLIENT_ERROR, false);
    }

    // Convenience method for 5xx
    public static void not5xx(Response response) {
        assertStatusInRange(response, HttpStatusRange.SERVER_ERROR, false);
    }

    public void validateStatusCode(Response response, int expected, String... message) {
        var msg = getFirstMessageOrDefault(message, ErrorConstants.API_RESPONSE_CODE_MISMATCH);
        logComparison("HTTP Status", expected, response.statusCode());
        Assert.assertEquals(response.statusCode(), expected, msg);
    }

    public void validateStatusCodeIn(Response response, int... expectedStatuses) {
        var msg = ErrorConstants.API_RESPONSE_CODE_MISMATCH;
        logComparison("HTTP Status IN", Arrays.toString(expectedStatuses), response.statusCode());
        Assert.assertTrue(Arrays.stream(expectedStatuses).anyMatch(s -> s == response.statusCode()), msg);
    }

    public void validateStatusCodeNotIn(Response response, int... expectedStatuses) {
        var msg = ErrorConstants.API_RESPONSE_CODE_MISMATCH;
        logComparison("HTTP Status IN", Arrays.toString(expectedStatuses), response.statusCode());
        Assert.assertTrue(Arrays.stream(expectedStatuses).noneMatch(s -> s == response.statusCode()), msg);
    }

    public void assertResponseContentType(Response response, ContentType contentType) {
        var actual = response.getHeader("Content-Type").toLowerCase();
        var expected = contentType.getContentTypeStrings()[0].toLowerCase();
        Assert.assertTrue(actual.contains(expected), ErrorConstants.API_RESPONSE_CONTENT_TYPE_MISMATCH);
    }

    /* ----------------------- JSON Assertions ----------------------- */

    public void validateObject(SoftAssert softAssert, Object expected, Object actual, String message, String... fieldsToIgnore) {
        logComparison("JSON", expected, actual);

        var customIgnores = Arrays.stream(fieldsToIgnore)
                .map(f -> new Customization(f, (o1, o2) -> true))
                .toArray(Customization[]::new);

        var result = JSONCompare.compareJSON(
                new JSONObject(expected),
                new JSONObject(actual),
                new CustomComparator(JSONCompareMode.LENIENT, customIgnores)
        );

        softAssert.assertFalse(result.failed(), message + " - " + result.getMessage());
    }



    /* ----------------------- Utility Assertions ----------------------- */

    public static boolean assertEqualIgnoreCase(String actual, String expected) {
        if (actual != null && expected != null) {
            var act = actual.toUpperCase().trim();
            var exp = expected.toUpperCase().trim();
            logComparisonStatic("IgnoreCase String", exp, act);
            return Objects.equals(act, exp);
        }
        logComparisonStatic("IgnoreCase String", expected, actual);
        return Objects.equals(actual, expected);
    }

    public <T> void assertEqual(T actual, T expected, SoftAssert softAssert, String msg) {
        if (expected != null) {
            softAssert.assertEquals(actual, expected, msg);
        }
    }

    /* ----------------------- Private Helpers ----------------------- */

    // Generic method: pass true for "in range", false for "not in range"
    private static void assertStatusInRange(Response response, HttpStatusRange range, boolean shouldBeInRange) {
        int status = response.statusCode();
        boolean inRange = range.contains(status);
        if (shouldBeInRange) {
            Assert.assertTrue(inRange, "Expected status in range " + range + " but was " + status);
        } else {
            Assert.assertFalse(inRange, "Expected status NOT in range " + range + " but was " + status);
        }
    }

    private static String getFirstMessageOrDefault(String[] messages, String defaultMsg) {
        return (messages != null && messages.length > 0) ? messages[0] : defaultMsg;
    }

    private void logComparison(String label, Object expected, Object actual) {
        var msg = String.format("%s -> EXPECTED: %s, ACTUAL: %s", label,
                jsonHelper.convertObjectToJsonString(expected),
                jsonHelper.convertObjectToJsonString(actual));
        log.info(msg);
        AllureLoggingUtils.logsToAllureReport(msg);
    }

    private static void logComparisonStatic(String label, Object expected, Object actual) {
        var msg = String.format("%s -> EXPECTED: %s, ACTUAL: %s", label, expected, actual);
        AllureLoggingUtils.logsToAllureReport(msg);
        log.info(msg);
    }

    // -------------------------------- Fluent SoftAssert -------------------------------------

    private static final ThreadLocal<SoftAssertFluent> threadLocalSoftAssert = new ThreadLocal<>();
    private static int creationCounter = 0; // static counter for all instances

    public static SoftAssertFluent softAssert() {
        if (threadLocalSoftAssert.get() == null) {
            threadLocalSoftAssert.set(new SoftAssertFluent(new SoftAssert()));
        }
        return threadLocalSoftAssert.get();
    }

    public static class SoftAssertFluent {
        private final SoftAssert softAssert;

        public SoftAssertFluent(SoftAssert softAssert) {
            creationCounter++; // increment whenever a new instance is created
            log.info("Created SoftAssert object, count = {}", creationCounter);
            this.softAssert = softAssert;
        }

        public SoftAssertFluent assertTrue(boolean condition, String message) {
            softAssert.assertTrue(condition, message);
            logComparisonStatic("SoftAssert assertTrue", message, condition);
            return this;
        }

        public SoftAssertFluent assertFalse(boolean condition, String message) {
            softAssert.assertFalse(condition, message);
            logComparisonStatic("SoftAssert assertFalse", message, condition);
            return this;
        }

        public <T> SoftAssertFluent assertEquals(T actual, T expected, String message) {
            softAssert.assertEquals(actual, expected, message);
            logComparisonStatic("SoftAssert assertEquals", "Expected: " + expected, "Actual: " + actual);
            return this;
        }

        public void assertAll() {
            try {
                softAssert.assertAll(); // this may throw AssertionError
            } finally {
                try {
                    Field errorsField = SoftAssert.class.getDeclaredField("m_errors");
                    errorsField.setAccessible(true);
                    ((Map<?, ?>) errorsField.get(softAssert)).clear();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
