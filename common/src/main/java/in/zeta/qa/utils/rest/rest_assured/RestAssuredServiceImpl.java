package in.zeta.qa.utils.rest.rest_assured;

import in.zeta.qa.utils.cuncurrency.SingletonFactory;
import in.zeta.qa.utils.rest.ApiRequest;
import in.zeta.qa.utils.rest.HttpClientService;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RestAssuredServiceImpl extends AbstractRestAssuredService {

    private RestAssuredServiceImpl() { }   // prevent external new

    public static HttpClientService getInstance() {
        return SingletonFactory.getInstance(RestAssuredServiceImpl.class);
    }

    @Override
    protected RequestSpecification given() {
        return RestAssured.given();
    }

    @Override
    protected RequestSpecification applyFilters(RequestSpecification req, String url, ApiRequest<?> restRequest) {
        return req
                .filter(new CurlLoggingFilter())
                .filter(new AllureRestAssured().setRequestAttachmentName(url));
    }


}
