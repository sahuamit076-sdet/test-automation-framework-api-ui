package in.zeta.qa.utils.rest;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class ApiResponse {

    int statusCode;
    Map<String, List<String>> headers;
    String body;


}
