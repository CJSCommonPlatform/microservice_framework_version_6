package uk.gov.justice.services.test.utils.http;

public class BaseUriProvider {

    public static final String INTEGRATION_HOST_KEY = "INTEGRATION_HOST_KEY";

    private static final String SCHEME = "http";
    private static final String PORT = "8080";

    public static String getBaseUri() {

        final String host = System.getProperty(INTEGRATION_HOST_KEY, "localhost");
        return SCHEME + "://" + host + ":" + PORT;
    }
}
