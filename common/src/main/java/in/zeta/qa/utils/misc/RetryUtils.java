package in.zeta.qa.utils.misc;


import in.zeta.qa.utils.exceptions.*;
import in.zeta.qa.constants.anotation.RetryOnFailure;
import in.zeta.qa.constants.anotation.Retryable;
import in.zeta.qa.utils.fileUtils.PropertyFileReader;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;

import java.net.ConnectException;
import java.net.SocketException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@Log4j2
public class RetryUtils {

    private static final String EXECUTE_WITH_RETRY = "executeWithRetry";

    public RetryPolicy<Object> getDefaultRetryPolicy(int delayInSeconds, int maxRetries) {
        return new RetryPolicy<>()
                .handle(ConnectException.class)
                .handle(SocketException.class)
                .handle(NoHttpResponseException.class)
                .withDelay(Duration.ofSeconds(delayInSeconds))
                .withMaxRetries(maxRetries);
    }




    @SneakyThrows
    public Response executeWithRetry(Retryable<Response> action) {
        // Get the calling method dynamically from the stack trace
        RetryOnFailure retry = getCallingMethodRetryAnnotation();
        // Check if the method has the @RetryOnFailure annotation
        if (Objects.isNull(retry)) {
            return action.execute();
        }
        log.error("Retrying API Call...");
        // Use Failsafe to execute the action with the retry policy
        // Retry if response status code is not 2xx
        return Failsafe.with(getRetryPolicyForErrorResponse(retry.delayInSeconds(), retry.count(), retry.statusCodes())).get(action::execute);
    }

    @SneakyThrows
    public Response executeWithRetryUntilMatchStrNotFound(Retryable<Response> action, String matchingStr) {
        // Get the calling method dynamically from the stack trace
        RetryOnFailure retry = getCallingMethodRetryAnnotation();
        // Check if the method has the @RetryOnFailure annotation
        if (Objects.isNull(retry)) {
            return action.execute();
        }
        log.error("Retrying API Call until match Str not found...");
        // Use Failsafe to execute the action with the retry policy
        return Failsafe.with(getRetryPolicyForMatchingStrNotFound(retry.delayInSeconds(), retry.count(), matchingStr)).get(action::execute);
    }

    private static RetryOnFailure getCallingMethodRetryAnnotation() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // 0 - getStackTrace
        // 1 - getCallingMethodRetryAnnotation
        // 2 - executeWithRetry
        // 3 - Caller API Method
        OptionalInt executeWithRetryIndex = IntStream.range(0, stackTrace.length)
                .filter(i -> stackTrace[i].getMethodName().contains(EXECUTE_WITH_RETRY)).findFirst();
        try {
            if (executeWithRetryIndex.isPresent()) {
                StackTraceElement element = stackTrace[executeWithRetryIndex.getAsInt() + 1];
                Class<?> clazz = Class.forName(element.getClassName());
                log.info("API Call :: {}", element.getMethodName());
                //METHOD LEVEL WILL BE PRECEDENCE
                RetryOnFailure retryOnFailureApiMethod = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(method -> method.getName().equalsIgnoreCase(element.getMethodName())
                                && method.isAnnotationPresent(RetryOnFailure.class))
                        .findFirst().map(method -> method.getAnnotation(RetryOnFailure.class)).orElse(null);
                if (retryOnFailureApiMethod == null && clazz.isAnnotationPresent(RetryOnFailure.class)) {
                    return clazz.getAnnotation(RetryOnFailure.class);
                }
                return retryOnFailureApiMethod;
            }

        } catch (ClassNotFoundException e) {
            log.error("Failed find caller API method while retrying...");
            e.printStackTrace();
        }
        return null;
    }

    private RetryPolicy<Object> getRetryPolicyForErrorResponse(int delayInSeconds, int maxRetries, int[] statusCodes) {
        return getDefaultRetryPolicy(delayInSeconds, maxRetries)
                .handleResultIf(response -> isExpectedResponse((Response) response, statusCodes)).handle(TachyonTestException.class);
    }

    private boolean isExpectedResponse(Response response, int[] statusCodes) {
        if(statusCodes.length == 1 && statusCodes[0] == 0) {
            return  response.statusCode() < HttpStatus.SC_OK && response.statusCode() >= HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
        return IntStream.of(statusCodes).anyMatch(code -> code == response.statusCode());
    }

    private RetryPolicy<Object> getRetryPolicyForMatchingStrNotFound(int delayInSeconds, int maxRetries, String title) {
        return getDefaultRetryPolicy(delayInSeconds, maxRetries).handleResultIf(response ->
                isMatchingStringNotFound((Response) response, title)).handle(TachyonTestException.class);
    }

    private boolean isMatchingStringNotFound(Response response, String title) {
        if (StringUtils.isEmpty(title) || Objects.isNull(response)){
            return false;
        }
        return !response.body().asString().contains(title);
    }

    public static <T> Optional<T> executeWithRetryForMethod(Supplier<Optional<T>> task, int attempts, int delayInSeconds) {
        for (int i = 1; i <= attempts; i++) {
            try {
                Optional<T> result = task.get();
                if (result.isPresent()) {
                    return result;
                }
                AllureLoggingUtils.logsToAllureReport("Attempt " + i + " returned empty, retrying...");
            } catch (Exception e) {
                AllureLoggingUtils.logsToAllureReport("Attempt " + i + " failed: " + e.getMessage());
            }

            if (i < attempts) {
                CommonUtilities.waitInSeconds(delayInSeconds);
            }
        }
        AllureLoggingUtils.logsToAllureReport("All " + attempts + " attempts failed. Returning empty.");
        return Optional.empty();
    }

    @SneakyThrows
    public static boolean retryUntilTrue(Integer retryCount, Integer waitInSecs, BooleanSupplier condition) {
        int attempts = Optional.ofNullable(retryCount).orElse(3);
        int waitTime;
        try {
            waitTime = Optional.ofNullable(waitInSecs).orElse(Integer.parseInt(PropertyFileReader.getPropertyValue("CMS_WAIT_TIME")));
        } catch (Exception e) {
            waitTime = 5;
        }
        while (attempts-- >= 0) {
            try {
                if (condition != null && condition.getAsBoolean()) {
                    return true;
                }
            } catch (NullPointerException npe) {
                log.error("NullPointerException while evaluating condition: " + npe);
            } catch (Exception e) {
                log.error("Unexpected exception during retry evaluation: " + e);
            }
            CommonUtilities.waitInSeconds(waitTime);
        }
        return false;
    }
}