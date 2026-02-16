package in.zeta.qa.service.kibana;

import in.zeta.qa.constants.anotation.RetryOnFailure;
import in.zeta.qa.utils.fileUtils.PropertyFileReader;
import in.zeta.qa.utils.misc.JsonHelper;
import in.zeta.qa.utils.misc.RetryUtils;
import in.zeta.qa.utils.rest.ApiResponse;
import in.zeta.qa.utils.rest.HttpMethod;
import in.zeta.qa.utils.rest.ApiRequest;

import java.util.Map;

class KibanaClient {

    private static final String COOKIE_HEADER = "security_authentication=Fe26.2**4f1f2132ca3e44e0de156315fe00d1ea1ef52838c0891420b97c2c2f762ba789*6j94DWzs8F7KpWm34lrhbw*PHCRm4OGqPGaKCFkIuY7kInyWUk1U8co11OeQbqEZyg76nf4SWGrweJdjahw_tVct0wzdQTecrzUJgJ5DXrdi8Gtl5Ew6BYm9bwPfj1zpJ-lCN0GQAQZuVuDP6VL4PdoLB-6Eoqe_04GOo82L6xpwvX1sYOJ5Z9oJauiS6-RTadstRirPSred-DLJSDPWMY1MktU8QX2iYIIyJMsOQcyikj7x5hjHTnA7fTG1PmCA-iAqpiKj_RAtJ3ly8g6rZfHjVmwhen5PviPdhtrYIb2bX5GMn3ezYfRXG9_RnIDqtWkkLnieUUA5hZhCz6dCNXTvLHcqVnsU0lwj-LHl-2cynnvZzjxVQldYwbm0QeO0YA1Nn-xuIOZZI2whBT7FCBfS1mJW4g0o3tGQfMJTNuJ5CYmbaE6NrXNw3UKfxQrOD0AmqP0T7qxvorT_HG5cmPEwWOmDvXARlRIOfG_CPRlXgmZ9U0okz-856iEb80aIOodo7NmDgF498xVMGenlZjcJ19Ntb-_AVzDG5NRnAbryCpvHe23JbI3pT_n7KaNhqc**e4712693c699b4ab7e49c226b93ac5ba2977723f3c7b1eebf8a358568a358c2a*seOEGEc1EGL0BKfpHMbngto6LfkdMSR7Ac95QWVFJns; STATE-TOKEN=0f5269bf-1bca-45ae-beba-3681384c821d; REFRESH-TOKEN=eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAifQ.VnlGOx97w5QOpIg4LAdGaC3D4SXQSmiyQaxk0jVLM2LYMpATEkktFT3p1vCDt4rlqXGYsYFHaSiehODGZPV7PLp_E5j0tpqPHqjHz9-5LdjYArkecufrzmaASqOG7ZNI7x7Oe7o7pj2k1fDr7gUlPJ0N-2a20dtMtYq4Yy-qut7SpGesKDWLoLRnMXKZM_th843V8nYAYlxtqQKOe3LgdXQ0Ehy948Wpyl2KXwZDXpSkVVWq1Ggrpw0c-V4Y3HaafNYmI-RGZZupesCTZiS2V9GrT83NjmZB95LRL-REb83pPnc1BKj6fBHuxOT9QHSE7yDXw8Qw7j7G834_sws1oA.BuljBA3is7r5R5I8.wMxLRqyTidIWvKXKuebzgcdjGa8XqpMx4mX6orSCzU9UtvYo6BqvOnBbVONMsD_R7eNtvgeiouOwaY3uM3KgZWkOIRYRLQLLugJKwc5UmC_aeO2i2kE3dSDv-_lHVi11tjfihs2OrCghKqcO8sxnozq5Slc5kPPkwfIAQ9h13hQ9OlfdKqso6MSFZpMR7FS_wvRzt-YO6UKsodfEL_jBsmez4hZZDPEz8i4WCWKAL5oXlxO0-Po-b_ltAIuM-DG5R-NQRFhXiwd7WL9eac4dXsSo0zhuWONt8l8IduCi7muEF7vA65d5NpCmHn0GOPxZVrkUBYJ-HT7KynDxM8v-HDJRvtrTW9OgnCaXoMLs99VB_e-VhuZEz0FqW6g-ooU2Iol7TjCMKHW675dCR0mtJS6XzIT8WbzCTtRjT3QYrS6VmIP99lJ1iDY-ebNfqHmxxqWz11nCw3AhwRxROyDC1OumrkvkiEx0ucOXwOTZk4Esvoo4LZE9fvfXlY5ynkRN4Dxptr9NIG8M9H_FzLzCaRkP94sCu8NKss4t2CXp7zXXREcwTahJ0OnDFxwI91bbzyZASXT5u4iXp0mU8ulRqEmWoXbFvCNms8pN6BcQlcjYFOdGAkJ8YeP7ZjoU4r_GIwmV27rvLJFarG3l2sO_bHWrPEd1DXqFg-P6hpe53WoUbdTff3EyEK1qpDnxwqrzBGLeq6WutOoivc0GeuK_8Y09YAcLyU5y3GK7vokcyG3L827abJMrtq_59nPnwtllOy9KCCvmqQKUVMp8-3aqz4NAcG_ti9UvzkORvyagp1at1E5yPika9mk2Xsrur4RehxT4iZ-X8_SB4OHBxYVE-p_XVvHVKomKvWlMNzGN1twBVp9SbKkjYrdLu2U_C_6cYu7zhQDlmgbtcuHOBdGv9gM8wH4sL3nS3LS3p8v6vPJ3BNE5t1uMjtCEp3pmdPz2Fofj_EI2I-gNfIQR2Znz-TsJ4IIyarD-cN0L8gO5Aw-dyPzlFvMtoFMeGdsCd8VMNWqnDlZw8OknsFROkWMGKmIEXm098C6S3cCl9y1KLsYuAuxvCeVDgSPTubo7Lk05yxpGy_gaSEM0qjsmCD2NoyWng3AJlwlZPtGLO9wBrzvWVMVwEUvn6-TUHWUEATCluW_8qloa6g0IdB_gD2bT0R_-4YDchTxypsr4-EffCe7J9c00-tONR2mnxwEtg7lxRpqvmBVhHJLvL0eDRPUZ3YzY4-fFXvNVlnF2CTsTRoXbmygHcg.qpzOFk4JLiYi60cz4QUqaA; ID-TOKEN=eyJraWQiOiJ3WWwrMXN5WklKM05pMUNHVGdVejlaWDZGb2dRSVNPdlpzRTdDd2RDOUxzPSIsImFsZyI6IlJTMjU2In0.eyJhdF9oYXNoIjoic1daUFF0TzhDMWlwaFRBUEpvamlnQSIsInN1YiI6IjgxYjMwZGVhLTMwODEtNzA0Ny04NjI0LWVlMGY3OWJhNWI0NiIsImNvZ25pdG86Z3JvdXBzIjpbImFwLXNvdXRoLTFfelpPVkNlclJ0X0NpcGhlck9JREMiXSwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAuYXAtc291dGgtMS5hbWF6b25hd3MuY29tXC9hcC1zb3V0aC0xX3paT1ZDZXJSdCIsImNvZ25pdG86dXNlcm5hbWUiOiJDaXBoZXJPSURDX0xTblg4clJfa1Jibm5WZnZaN0NDa2c9PSIsInByZWZlcnJlZF91c2VybmFtZSI6ImFzaGlzaHJhc0B6ZXRhLnRlY2giLCJvcmlnaW5fanRpIjoiZTUwMDIzZDEtM2M4Mi00YjIwLWEwZGQtYmQyMWJmMzdjZDZhIiwiYXVkIjoicm10YWtobmlqb2dvN2lnb2Z2ZTA4cG1tNCIsImlkZW50aXRpZXMiOlt7ImRhdGVDcmVhdGVkIjoiMTcxNzY2NjQyNDQ1NCIsInVzZXJJZCI6IkxTblg4clJfa1Jibm5WZnZaN0NDa2c9PSIsInByb3ZpZGVyTmFtZSI6IkNpcGhlck9JREMiLCJwcm92aWRlclR5cGUiOiJPSURDIiwiaXNzdWVyIjpudWxsLCJwcmltYXJ5IjoidHJ1ZSJ9XSwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE3NTQ4OTI0MDQsIm5hbWUiOiJBc2hpc2ggS3VtYXIgUmFzdG9naSIsImV4cCI6MTc1NDk4OTYxOCwiaWF0IjoxNzU0OTg2MDE4LCJqdGkiOiJiMDRkZjVmZC05MThhLTRhMjMtYjU2NC1hNGI1NDYxYWNkYzkiLCJlbWFpbCI6ImFzaGlzaHJhc0B6ZXRhLnRlY2gifQ.hGEVABHWp2La38KMfeSe0agSZw-HBhD778Loiw9aXncah18UKrYSLsCFhg5RkWZ4cE2TPsrQWGegxqDQM1i7EHB-CaW4PW4InBlfp-rh6tysCejVHev00ErxHv2jwj8rjVZCIgXC5Yq23JvuHye36zwhKHDgmODeRR29zY0Fn50_na2aOqfonzVRA8x7wC4L4DL94ggz3z0lJJygCnVM3Nf34F5GhEW8XmrYNj2F2GZs16Ut7ZZxaaUlfucEhOw23jzvBaDifTEjfdzpD77Fi3DNngs1lBpnxJ5yQPe3NFYtFTw3jjOiTxxVY3elNiyApVCeKhqMDrq6Ou8BHX4LVw; ACCESS-TOKEN=eyJraWQiOiIybkltRElJK0hQRnNVVHNvKzdRbG92MXdwSW1vNG9jS011K0JQT0NnUnRVPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiI4MWIzMGRlYS0zMDgxLTcwNDctODYyNC1lZTBmNzliYTViNDYiLCJjb2duaXRvOmdyb3VwcyI6WyJhcC1zb3V0aC0xX3paT1ZDZXJSdF9DaXBoZXJPSURDIl0sImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC5hcC1zb3V0aC0xLmFtYXpvbmF3cy5jb21cL2FwLXNvdXRoLTFfelpPVkNlclJ0IiwidmVyc2lvbiI6MiwiY2xpZW50X2lkIjoicm10YWtobmlqb2dvN2lnb2Z2ZTA4cG1tNCIsIm9yaWdpbl9qdGkiOiJlNTAwMjNkMS0zYzgyLTRiMjAtYTBkZC1iZDIxYmYzN2NkNmEiLCJ0b2tlbl91c2UiOiJhY2Nlc3MiLCJzY29wZSI6InBob25lIG9wZW5pZCBwcm9maWxlIGVtYWlsIiwiYXV0aF90aW1lIjoxNzU0ODkyNDA0LCJleHAiOjE3NTQ5ODk2MTgsImlhdCI6MTc1NDk4NjAxOCwianRpIjoiYmM2Nzk3M2ItMDVmNi00MzEzLWEzYjAtMWFjOTdlMDQ1MzA2IiwidXNlcm5hbWUiOiJDaXBoZXJPSURDX0xTblg4clJfa1Jibm5WZnZaN0NDa2c9PSJ9.ytYpYsr5QTvYlGaivCCSwRxkT3G0GV08GCAw1XZ5YPgIRINm_xsM9IBSq8_yeVvUIElew4QYfDIX2N0BcCZEQu890scbDdPG-Bpof8IQXHGLQh_KcLuSrDr4VUmdUQ-c9DvUcv2qjli7qcpuNG8EZrNVuKLuCKtGQ8qTP54VKw4XHPKil4ZbciKEXLlTGY9VSUlRolxaE6OIqrOZE1GxBMx-qb1MtBm8oEOMGV_qh7WaxgAf8qnCeX4K3jTOO_Xexk3k18XMQvzPdJY63S1RNN7AxLP4ciSzM_Y6hPkeUYMTjZnSAMRfE5pT0NDDheGbpI93wk8BnDSchx_H1qSliA";
    private static final Map.Entry<String, String> CONTENT_TYPE_JSON = Map.entry("Content-Type", "application/json");
    private static final Map.Entry<String, String>  HEADER_XSRF = Map.entry("osd-xsrf", "true");

    private final JsonHelper jsonHelper = new JsonHelper();
    private final String kibanaServer;
    private final String cookieHeader;

    public KibanaClient() {
        this.kibanaServer = PropertyFileReader.getPropertyValue("kibana.server.url");
        this.cookieHeader = COOKIE_HEADER; // TODO(AMIT): load from config instead
    }

    private Map<String, String> getDefaultHeaders() {
        return  Map.ofEntries(CONTENT_TYPE_JSON, HEADER_XSRF, Map.entry("cookie", cookieHeader));
    }

    public ApiResponse openSearch(String payload) {
        return ApiRequest.<String>builder()
                .serverURL(kibanaServer)
                .endpoint(KibanaEndpoint.SEARCH)
                .headers(getDefaultHeaders())
                .body(payload)
                .method(HttpMethod.POST).execute();
    }

    @RetryOnFailure(count = 10, delayInSeconds = 5)
    public ApiResponse openSearchWithRetryUntilMatchedStrFound(String payload, String title) {
        ApiRequest<String> request = ApiRequest.<String>builder()
                .serverURL(kibanaServer)
                .endpoint(KibanaEndpoint.SEARCH)
                .headers(getDefaultHeaders())
                .body(payload)
                .method(HttpMethod.POST)
                .build();
        return new RetryUtils().executeWithRetryUntilMatchStrNotFound(request::execute, title);
    }

    public ApiResponse shortUrl(String longUrlPath) {
        Map<String, String> map = Map.of("url", longUrlPath);
        String payload = jsonHelper.convertObjectToJsonString(map);
        return ApiRequest.<String>builder()
                .serverURL(kibanaServer)
                .endpoint(KibanaEndpoint.SHORT_URL)
                .headers(getDefaultHeaders())
                .body(payload)
                .method(HttpMethod.POST).execute();
    }
}
