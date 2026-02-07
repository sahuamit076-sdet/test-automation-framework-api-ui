package in.zeta.qa.utils.testListeners;

import in.zeta.qa.constants.ErrorConstants;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.Objects;

public class AuthorizationTimeoutRetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 2; // Maximum number of retries

    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess() && retryCount < MAX_RETRY_COUNT) {
            if (isAuthorizationTimedOut(result)) {
                retryCount++;
                return true; // Retry the test
            }
        }
        return false; // Do not retry
    }

    private boolean isAuthorizationTimedOut(ITestResult result) {
        return Objects.nonNull(result.getThrowable().getMessage()) &&
                result.getThrowable().getMessage().contains(ErrorConstants.AUTH_TIME_OUR_ERROR);
    }
}
