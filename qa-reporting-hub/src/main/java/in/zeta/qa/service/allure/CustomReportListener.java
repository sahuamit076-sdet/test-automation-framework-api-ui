package in.zeta.qa.service.allure;

import in.zeta.qa.constants.CommonConstants;
import in.zeta.qa.utils.fileUtils.PropertyFileReader;
import lombok.extern.slf4j.Slf4j;
import org.testng.IExecutionListener;


@Slf4j
public class CustomReportListener implements IExecutionListener {

    @Override
    public void onExecutionFinish() {
        log.info("Optimizing Allure Report for better readability...");
        boolean searchKibanaLogs = PropertyFileReader.getPropertyValue("environment").equalsIgnoreCase(CommonConstants.SHOWROOM_ZONE);
        AllureUtil.getInstance().optimizeNewFailures(true, searchKibanaLogs, true);
    }
}
