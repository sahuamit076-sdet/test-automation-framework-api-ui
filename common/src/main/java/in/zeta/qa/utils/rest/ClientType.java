package in.zeta.qa.utils.rest;

import in.zeta.qa.utils.rest.ok_http.OkHttpServiceImpl;
import in.zeta.qa.utils.rest.rest_assured.RestAssuredSerenityServiceImpl;
import in.zeta.qa.utils.rest.rest_assured.RestAssuredServiceImpl;

import java.util.function.Supplier;

public enum ClientType {

    REST_ASSURED(RestAssuredServiceImpl::getInstance),
    OK_HTTP(OkHttpServiceImpl::getInstance),
    REST_ASSURED_SERENITY(RestAssuredSerenityServiceImpl::getInstance);

    private final Supplier<HttpClientService> supplier;

    ClientType(Supplier<HttpClientService> supplier) {
        this.supplier = supplier;
    }

    public HttpClientService getService() {
        return supplier.get();
    }


}
