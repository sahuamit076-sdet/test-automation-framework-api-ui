package in.zeta.qa.utils.rest;

import io.restassured.response.Response;

 interface HttpClientService {
    
    Response execute(ApiRequest<?> request);

}
