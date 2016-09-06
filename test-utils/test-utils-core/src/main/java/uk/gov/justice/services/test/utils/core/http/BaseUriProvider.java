package uk.gov.justice.services.test.utils.core.http;


import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

public class BaseUriProvider {


    private static final String SCHEME = "http";
    private static final String PORT = "8080";

    public static String getBaseUri() {

        return SCHEME + "://" + getHost() + ":" + PORT;
    }
}
