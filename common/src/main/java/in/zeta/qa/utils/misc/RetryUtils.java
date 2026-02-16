package in.zeta.qa.utils.misc;


import in.zeta.qa.utils.exceptions.*;
import in.zeta.qa.constants.anotation.RetryOnFailure;
import in.zeta.qa.constants.anotation.Retryable;
import in.zeta.qa.utils.rest.ApiResponse;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NoHttpResponseException;

import java.net.ConnectException;
import java.net.SocketException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.OptionalInt;
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
    public ApiResponse executeWithRetry(Retryable<ApiResponse> action) {
        RetryOnFailure retry = getCallingMethodRetryAnnotation();
        if (Objects.isNull(retry)) {
            return action.execute();
        }
        log.error("Retrying API Call...");
        return Failsafe.with(getRetryPolicyForErrorResponse(retry.delayInSeconds(), retry.count(), retry.statusCodes())).get(action::execute);
    }

    @SneakyThrows
    public ApiResponse executeWithRetryUntilMatchStrNotFound(Retryable<ApiResponse> action, String matchingStr) {
        RetryOnFailure retry = getCallingMethodRetryAnnotation();
        if (Objects.isNull(retry)) {
            return action.execute();
        }
        log.error("Retrying API Call until match Str not found...");
        return Failsafe.with(getRetryPolicyForMatchingStrNotFound(retry.delayInSeconds(), retry.count(), matchingStr)).get(action::execute);
    }

    public static RetryOnFailure getCallingMethodRetryAnnotation() {
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
                .handleResultIf(response -> isExpectedResponse((ApiResponse) response, statusCodes)).handle(TachyonTestException.class);
    }

    private boolean isExpectedResponse(ApiResponse response, int[] statusCodes) {
        if (statusCodes.length == 1 && statusCodes[0] == 0) {
            return response.getStatusCode() < 100 && response.getStatusCode() >= 500;
        }
        return IntStream.of(statusCodes).anyMatch(code -> code == response.getStatusCode());
    }

    private RetryPolicy<Object> getRetryPolicyForMatchingStrNotFound(int delayInSeconds, int maxRetries, String title) {
        return getDefaultRetryPolicy(delayInSeconds, maxRetries).handleResultIf(response ->
                isMatchingStringNotFound((Response) response, title)).handle(TachyonTestException.class);
    }

    private boolean isMatchingStringNotFound(Response response, String title) {
        if (StringUtils.isEmpty(title) || Objects.isNull(response)) {
            return false;
        }
        return !response.body().asString().contains(title);
    }




}