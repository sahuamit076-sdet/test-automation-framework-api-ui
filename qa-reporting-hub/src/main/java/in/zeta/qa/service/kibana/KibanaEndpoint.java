package in.zeta.qa.service.kibana;

import in.zeta.qa.constants.endpoints.ApiEndpoint;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum KibanaEndpoint implements ApiEndpoint {

    SEARCH("/_dashboards/internal/search/opensearch"),
    SHORT_URL("/_dashboards/api/shorten_url");

    private final String path;
}
