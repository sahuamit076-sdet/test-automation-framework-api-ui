package in.zeta.qa.utils.rest;

import in.zeta.qa.constants.endpoints.ApiEndpoint;

import java.util.Optional;

public interface HttpClientService {
    
    ApiResponse execute(ApiRequest<?> request);


     default String buildUrl(ApiRequest<?> request) {
         String resolved = Optional.ofNullable(request.getEndpoint())
                 .map(ApiEndpoint::getPath)
                 .orElse("");

         if (request.getPathParams() != null) {
             for (var entry : request.getPathParams().entrySet()) {
                 resolved = resolved.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
             }
         }

         return request.getServerURL() + resolved;
     }
}
