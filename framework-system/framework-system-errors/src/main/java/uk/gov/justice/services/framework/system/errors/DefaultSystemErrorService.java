package uk.gov.justice.services.framework.system.errors;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.framework.system.errors.SystemErrorService;
import uk.gov.justice.services.framework.utilities.exceptions.StackTraceProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.system.domain.EventError;
import uk.gov.justice.services.system.persistence.EventErrorLogRepository;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class DefaultSystemErrorService implements SystemErrorService {

    private static final Long MISSING_EVENT_NUMBER = -1L;
    private static final String NO_COMMENT = "";

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
        final Optional<Long> eventNumber = metadata.eventNumber();

        final EventError eventError = new EventError(
                messageId,
                componentName,
                eventId,
                eventNumber.orElse(MISSING_EVENT_NUMBER),
                metadata.asJsonObject().toString(),
                jsonEnvelope.payload().toString(),
                exception.getMessage(),
                stackTraceProvider.getStackTrace(exception),
                clock.now(),
                getComment(eventNumber)
        );

        eventErrorLogRepository.save(eventError);
    }

    private String getComment(final Optional<Long> eventNumberOptional) {

        if (eventNumberOptional.isPresent()) {
            return NO_COMMENT;
        }

        return format("Event number is missing from event. Setting to %d instead", MISSING_EVENT_NUMBER);
    }
}
