package in.zeta.qa.utils.misc;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.Attachment;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public final class AllureLoggingUtils {

    public synchronized static void updateTestMethodNameAndDescription(String description) {
        updateTestMethodName(description);
        updateTestDescription(description);
    }

    public synchronized static void updateTestMethodName(String methodName) {
        Allure.getLifecycle().updateTestCase(tc -> tc.setName(methodName));
    }

    public synchronized static void updateTestDescription(String description) {
        Allure.description(description);
    }

    public static void log(String message) {
        Allure.attachment(message, "");
    }

    public static void log(String message, String content) {
        Allure.attachment(message, content);
    }

    @Attachment(value = "Page Screenshot", type = "image/png")
    public static byte[] attachScreenshot(WebDriver driver) {
        TakesScreenshot ts = (TakesScreenshot) driver;
        return ts.getScreenshotAs(OutputType.BYTES);
    }

    public static void logToAllureAndConsole(Logger logger, String... messages) {
        StringBuilder combinedMessage = new StringBuilder();
        try {
            for (String message : messages) {
                combinedMessage.append(message);
            }
            String finalMessage = combinedMessage.toString();
            logsToAllureReport(finalMessage);
            logger.info(finalMessage);
        } catch (Exception e) {
            logger.error("unable to log data");
        }
    }

    public static void logsToAllureReport(String message) {
        Allure.attachment(message, "");
    }

    public static void logsToAllureReport(String message, String content) {
        Allure.attachment(message, content);
    }

    public static void classNameAllureReport(String message) {
        AllureLifecycle lifecycle = Allure.getLifecycle();
        lifecycle.updateTestCase(testResult -> testResult.setName(message));
    }

}
