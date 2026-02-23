package in.zeta.qa;

import in.zeta.qa.utils.rest.ClientType;
import org.testng.annotations.Test;

public class Tests {

    @Test
    void testRestAssured() {
        ApiClientHelper.callApi(ClientType.REST_ASSURED);
    }

    @Test
    void testRestAssuredSerenity() {
        ApiClientHelper.callApi(ClientType.REST_ASSURED_SERENITY);
    }
}
