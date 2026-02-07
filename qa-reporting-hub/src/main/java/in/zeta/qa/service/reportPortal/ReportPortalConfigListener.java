package in.zeta.qa.service.reportPortal;

import com.epam.reportportal.testng.BaseTestNGListener;

public class ReportPortalConfigListener extends BaseTestNGListener {


    public ReportPortalConfigListener() {
        super(new ReportPortalOverrideParameters());
    }
}
