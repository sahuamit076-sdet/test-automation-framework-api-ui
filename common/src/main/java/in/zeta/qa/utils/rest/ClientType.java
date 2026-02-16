package in.zeta.qa.utils.rest;

import in.zeta.qa.utils.rest.ok_http.OkHttpServiceImpl;
import in.zeta.qa.utils.rest.rest_assured.RestAssuredServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClientType {

    REST_ASSURED(new RestAssuredServiceImpl()),
    OK_HTTP(new OkHttpServiceImpl());

    private final HttpClientService service;



}
