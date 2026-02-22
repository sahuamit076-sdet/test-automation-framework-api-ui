package in.zeta.qa.utils.rest.rest_assured;

import in.zeta.qa.utils.cuncurrency.SingletonFactory;
import in.zeta.qa.utils.rest.ApiRequest;
import in.zeta.qa.utils.rest.HttpClientService;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;


public class RestAssuredSerenityServiceImpl extends AbstractRestAssuredService {

    public static HttpClientService getInstance() {
        return SingletonFactory.getInstance(RestAssuredSerenityServiceImpl.class);
    }

    @Override
    protected RequestSpecification given() {
        return SerenityRest.given();
    }

    @Override
    protected RequestSpecification applyFilters(RequestSpecification req, String url, ApiRequest<?> restRequest) {
        return req.filter(new CurlLoggingFilter());
    }
}
