package uk.gov.justice.services.test.utils.core.messaging;

import static java.lang.String.format;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

public class QueueUriProvider {

    private static final String BASE_URI_PATTERN = "tcp://%s:61616";

    public String getQueueUri() {
        return format(BASE_URI_PATTERN, getHost());
    }

    public static String queueUri() {
        return new QueueUriProvider().getQueueUri();
    }
}
