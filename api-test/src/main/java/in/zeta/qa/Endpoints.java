package in.zeta.qa;

import in.zeta.qa.constants.endpoints.ApiEndpoint;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Endpoints implements ApiEndpoint {
    FIND_BY_STATUS("/v2/pet/findByStatus?status={statusValue}");

    private final String path;

}
