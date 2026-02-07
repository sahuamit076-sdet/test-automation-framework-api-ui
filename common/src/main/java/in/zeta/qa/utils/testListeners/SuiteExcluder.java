package in.zeta.qa.utils.testListeners;

import in.zeta.qa.constants.CommonConstants;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

import java.util.List;

public class SuiteExcluder implements IAlterSuiteListener {

    @Override
    public void alter(List<XmlSuite> suites) {
        String excludeSuite = System.getProperty(CommonConstants.EXCLUDE_SUITE);
        if (excludeSuite != null && !excludeSuite.isEmpty()) {
            for (XmlSuite suite : suites) {
                List<String> suiteFiles = suite.getSuiteFiles();
                suiteFiles.removeIf(suiteFile -> suiteFile.contains(excludeSuite));
            }
        }
    }
}
