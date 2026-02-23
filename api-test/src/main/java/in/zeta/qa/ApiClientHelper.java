package in.zeta.qa;

import in.zeta.qa.utils.misc.CommonUtilities;
import in.zeta.qa.utils.rest.ApiRequest;
import in.zeta.qa.utils.rest.ApiResponse;
import in.zeta.qa.utils.rest.ClientType;
import in.zeta.qa.utils.rest.HttpMethod;

import java.util.Map;

public class ApiClientHelper {


    public static ApiResponse callApi(ClientType clientType) {
        CommonUtilities.waitInSeconds(2);
        return ApiRequest.<Void>builder()
                .client(clientType)
                .serverURL("https://petstore.swagger.io")
                .endpoint(Endpoints.FIND_BY_STATUS)
                .pathParams(Map.of("statusValue", "available"))
                .method(HttpMethod.GET)
                .build().execute();
    }
}
