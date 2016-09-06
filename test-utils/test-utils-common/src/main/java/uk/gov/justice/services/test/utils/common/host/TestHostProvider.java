package uk.gov.justice.services.test.utils.common.host;

public class TestHostProvider {

    public static final String INTEGRATION_HOST_KEY = "INTEGRATION_HOST_KEY";

    public static String getHost() {
        return System.getProperty(INTEGRATION_HOST_KEY, "localhost");
    }
}
