package in.zeta.qa.service.reportPortal;

import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import org.testng.ITestResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestAttributeProcessor {

    public static void setCustomTestAttributes(ITestResult result, Map<String, String> customAttributes) {
        Set<ItemAttributesRQ> attributes = new HashSet<>();
        for (Map.Entry<String, String> entry : customAttributes.entrySet()) {
            attributes.add(new ItemAttributesRQ(entry.getKey(), entry.getValue()));
        }
        result.setAttribute("customTestAttributes", attributes);
    }

    public static void resetTestName(ITestResult result, String newTestName) {
        result.setAttribute("scenarioName", newTestName);
    }
}

