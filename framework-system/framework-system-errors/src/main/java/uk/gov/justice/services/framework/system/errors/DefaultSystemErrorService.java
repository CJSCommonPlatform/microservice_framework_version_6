package uk.gov.justice.services.framework.system.errors;

import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.framework.utilities.exceptions.StackTraceProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.system.domain.EventError;
import uk.gov.justice.services.system.persistence.EventErrorLogRepository;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class DefaultSystemErrorService implements SystemErrorService {

    @Inject
    private EventErrorLogRepository eventErrorLogRepository;

    @Inject
    private StackTraceProvider stackTraceProvider;

    @Inject
    private UtcClock clock;

    @Override
    @Transactional(NOT_SUPPORTED)
    public void reportError(
            final String messageId,
            final String componentName,
            final JsonEnvelope jsonEnvelope,
            final Throwable exception) {

        final Metadata metadata = jsonEnvelope.metadata();
        final UUID eventId = metadata.id();
        final String eventName = metadata.name();
        final Optional<Long> eventNumber = metadata.eventNumber();
        final Optional<UUID> streamId = metadata.streamId();

        final EventError eventError = new EventError(
                messageId,
                componentName,
                eventName,
                eventId,
                streamId,
                eventNumber,
                metadata.asJsonObject().toString(),
                jsonEnvelope.payload().toString(),
                exception.getMessage(),
                stackTraceProvider.getStackTrace(exception),
                clock.now()
        );

        eventErrorLogRepository.save(eventError);
    }
}
