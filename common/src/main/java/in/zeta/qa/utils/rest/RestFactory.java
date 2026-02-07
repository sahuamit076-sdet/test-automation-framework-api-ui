package in.zeta.qa.utils.rest;


public final class RestFactory {

    private RestFactory() {
        // prevent instantiation
    }

    // Global reusable client (singleton)
    private static final RestClientServiceImpl SINGLETON = new RestClientServiceImpl();

    // Per-thread client (lazy, created on demand)
    private static final ThreadLocal<RestClientServiceImpl> THREAD_LOCAL =
            ThreadLocal.withInitial(RestClientServiceImpl::new);

    public static HttpClientService getClient() {
        return SINGLETON; // loaded only when first accessed
    }

    public static HttpClientService getThreadLocalClient() {
        return THREAD_LOCAL.get();
    }
}
