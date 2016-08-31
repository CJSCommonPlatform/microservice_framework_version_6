package uk.gov.justice.services.test.utils.core.messaging;

import static java.lang.String.format;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.INTEGRATION_HOST_KEY;

public class QueueUriProvider {

    public static final String INTEGRATION_HOST_KEY = "INTEGRATION_HOST_KEY";
    private static final String BASE_URI_PATTERN = "tcp://%s:61616";

    public String getQueueUri() {

        final String host = System.getProperty(INTEGRATION_HOST_KEY, "localhost");

        return format(BASE_URI_PATTERN, host);
    }

    public static String queueUri() {
        return new QueueUriProvider().getQueueUri();
    }
}
