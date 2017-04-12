package uk.gov.justice.services.adapter.direct;

import uk.gov.justice.services.messaging.JsonEnvelope;

public interface SynchronousDirectAdapter {
    JsonEnvelope process(JsonEnvelope envelope);
}
