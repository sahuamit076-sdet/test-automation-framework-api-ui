package in.zeta.qa.utils.rest;

import in.zeta.qa.constants.endpoints.ApiEndpoint;
import java.util.Objects;
import java.util.Optional;

public interface HttpClientService {

    ApiResponse execute(ApiRequest<?> request);


    default String buildUrl(ApiRequest<?> request) {
        String base = Objects.requireNonNull(request.getServerURL(), "serverURL is required");


        String pathTemplate = Optional.ofNullable(request.getEndpoint())
                .map(ApiEndpoint::getPath).orElse("");

        if (request.getPathParams() != null) {
            for (var entry : request.getPathParams().entrySet()) {
                pathTemplate = pathTemplate
                        .replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }

        return stripTrailingSlash(base) + stripTrailingSlash(pathTemplate);
    }

    private String stripTrailingSlash(String s) {
        if (s == null) return null;
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }
}
