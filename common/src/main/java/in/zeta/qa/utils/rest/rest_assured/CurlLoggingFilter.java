package in.zeta.qa.utils.rest.rest_assured;

import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.internal.support.Prettifier;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CurlLoggingFilter implements OrderedFilter {

    final Prettifier prettifier = new Prettifier();


    /**
     * Logs the given RestAssured request as a cURL command
     *
     * @return
     */
    @Override
    public Response filter(final FilterableRequestSpecification requestSpec,
                           final FilterableResponseSpecification responseSpec,
                           final FilterContext filterContext) {

        StringBuilder curl = new StringBuilder("curl -X ")
                .append(requestSpec.getMethod())
                .append(" \\\n  '")
                .append(requestSpec.getURI())
                .append("'");

        // pretty headers
        requestSpec.getHeaders().forEach(h ->
                curl.append(" \\\n  -H '").append(h.getName())
                        .append(": ").append(h.getValue()).append("'"));

        // pretty body
        if (requestSpec.getBody() != null) {
            String body = prettifier.getPrettifiedBodyIfPossible(requestSpec);
            curl.append(" \\\n  -d '").append(body).append("'");
        }

        // form params support
        if (requestSpec.getFormParams() != null && !requestSpec.getFormParams().isEmpty()) {
            requestSpec.getFormParams().forEach((key, value) ->
                    curl.append(" \\\n  --form '")
                            .append(key).append("=\"").append(value).append("\"'"));
        }

        log.info("\n{}", curl);

        return filterContext.next(requestSpec, responseSpec);
    }


    /**
     * @return The order of the filter
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
