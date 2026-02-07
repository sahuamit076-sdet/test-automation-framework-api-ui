package in.zeta.qa.service.reportPortal;

import com.epam.reportportal.listeners.ItemStatus;
import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.testng.TestNGService;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import in.zeta.qa.utils.fileUtils.PropertyFileReader;
import lombok.extern.log4j.Log4j2;
import org.testng.ITestResult;
import org.testng.util.Strings;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Log4j2
public class ReportPortalOverrideParameters extends TestNGService {

    private static final String REPORT_PORTAL_HOST = "https://itpautomation.internal.mum1-pp.zetaapps.in/";
    private static final String REPORT_PORTAL_API_KEY = "api_key";
    private static final String REPORT_PORTAL_LAUNCH_NAME = "launch_name";
    private static final String REPORT_PORTAL_PROJECT_NAME = "project_name";

    public ReportPortalOverrideParameters() {
        super(getReportPortalProperties());
    }

    /***
     * Property name: rp.enable
     * Type: Boolean
     * Description: Enable/Disable logging to ReportPortal: rp.enable=true
     * - enable log to RP server. Any other value means 'false': rp.enable=false
     * - disable log to RP server. If parameter is absent in properties file then
     * automation project results will be posted on RP.
     */
    public static Supplier<Launch> getReportPortalProperties() {
        ListenerParameters parameters = new ListenerParameters(PropertiesLoader.load());
        parameters.setBaseUrl(REPORT_PORTAL_HOST);
        parameters.setApiKey(System.getenv(REPORT_PORTAL_API_KEY));
        parameters.setLaunchName(System.getenv(REPORT_PORTAL_LAUNCH_NAME));
        parameters.setProjectName(System.getenv(REPORT_PORTAL_PROJECT_NAME));
        boolean isReportPortalPushEnabled = Objects.equals(
                Objects.nonNull(System.getenv("BRANCH"))  ? System.getenv("BRANCH") :
                        Objects.nonNull(System.getenv("branch")) ? System.getenv("branch") :
                                System.getenv("Branch"),
                PropertyFileReader.getPropertyValue("rp.main.branch")
        );
        parameters.setEnable(isReportPortalPushEnabled);
        ReportPortal reportPortal = ReportPortal.builder().withParameters(parameters).build();
        StartLaunchRQ startLaunch = buildReportPortalLaunch(reportPortal.getParameters());
        return () -> reportPortal.newLaunch(startLaunch);
    }

    private static StartLaunchRQ buildReportPortalLaunch(ListenerParameters parameters) {
        StartLaunchRQ launchStart = new StartLaunchRQ();
        launchStart.setName(parameters.getLaunchName());
        launchStart.setStartTime(Calendar.getInstance().getTime());
        launchStart.setAttributes(parameters.getAttributes());
        launchStart.setMode(parameters.getLaunchRunningMode());
        if (!Strings.isNullOrEmpty(parameters.getDescription()))
            launchStart.setDescription(parameters.getDescription());
        return launchStart;
    }

    /**
     * Creates a step name for the test method. This step name will be used in ReportPortal to represent the test step.
     * If the test has a custom scenario name attribute, it will be used as the step name. Otherwise, the method name
     * of the test is used.
     *
     * @param testResult The result of the test execution, which contains the test details.
     * @return The name of the test step, either as a custom scenario name or the test method name.
     */
    @Override
    protected String createStepName(ITestResult testResult) {
        return Optional.ofNullable((String) testResult.getAttribute("scenarioName"))
                .orElse(testResult.getMethod().getMethodName());
    }

    /**
     * Builds the FinishTestItemRQ request object used to signal the end of a test step to ReportPortal.
     * This includes setting the end time, status, and any custom test attributes.
     *
     * @param status      The status of the test (e.g., PASSED, FAILED).
     * @param testResult  The ITestResult object representing the completed test method.
     * @return A populated FinishTestItemRQ request object to be sent to ReportPortal.
     */
    @Nonnull
    @Override
    protected FinishTestItemRQ buildFinishTestMethodRq(@Nonnull ItemStatus status, @Nonnull ITestResult testResult) {
        FinishTestItemRQ finishTestItemRq = new FinishTestItemRQ();
        finishTestItemRq.setEndTime(new Date(testResult.getEndMillis()));
        finishTestItemRq.setStatus(status.name());
        Optional.ofNullable(testResult.getAttribute("customTestAttributes"))
                .filter(Set.class::isInstance)
                .map(Set.class::cast)
                .map(set -> ((Set<?>) set).stream()
                        .filter(ItemAttributesRQ.class::isInstance)
                        .map(ItemAttributesRQ.class::cast)
                        .collect(Collectors.toSet()))
                .filter(collected -> !collected.isEmpty())
                .filter(collected -> ((Set<?>) testResult.getAttribute("customTestAttributes")).size() == collected.size())
                .ifPresent(finishTestItemRq::setAttributes);
        return finishTestItemRq;
    }
}
