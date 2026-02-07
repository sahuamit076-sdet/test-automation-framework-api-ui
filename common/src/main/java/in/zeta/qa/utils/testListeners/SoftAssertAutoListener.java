package in.zeta.qa.utils.testListeners;

import in.zeta.qa.utils.misc.AssertHelper;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class SoftAssertAutoListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        // Do nothing here; do NOT clear the SoftAssert
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        tryAssertAll();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        tryAssertAll();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        tryAssertAll();
    }

    private void tryAssertAll() {
        try {
            AssertHelper.softAssert().assertAll();
        } catch (AssertionError e) {
            // rethrow to fail the test if there were soft assertion failures
            throw e;
        }
    }

    @Override
    public void onStart(ITestContext context) { }

    @Override
    public void onFinish(ITestContext context) { }
}
