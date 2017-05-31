package uk.gov.justice.services.test.utils.common.host;

public class TestHostProvider {

    public static final String INTEGRATION_HOST_KEY = "INTEGRATION_HOST_KEY";
    public static final String ARTEMIS_HOST_KEY = "ARTEMIS_HOST_KEY";

    public static String getHost() {
        return System.getProperty(INTEGRATION_HOST_KEY, "localhost");
    }

    public static String getArtemisHost() {
        return System.getProperty(ARTEMIS_HOST_KEY, "localhost");
    }
}
