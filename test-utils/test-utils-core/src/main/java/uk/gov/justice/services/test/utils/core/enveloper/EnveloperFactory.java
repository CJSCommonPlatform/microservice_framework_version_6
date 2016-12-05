package uk.gov.justice.services.test.utils.core.enveloper;

import static java.util.Arrays.stream;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.extension.EventFoundEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for those who need to have a real Enveloper in their tests.
 *
 * To use:
 * <pre>
 *  {@code
 *
 *      "@Spy"
 *      private Enveloper enveloper = EnveloperFactory.createEnveloperWithEvents(RecipeAdded.class,
 * RecipeRemoved.class)
 *
 *      "@Mock"
 *      private MyDependency dependency;
 *
 *      "@InjectMocks"
 *      private MyClassUnderTest classUnderTest;
 * }
 * </pre>
 *
 * Mockito will now inject the real Enveloper into your test class along with any other mocks.
 */
public class EnveloperFactory {

    public static Enveloper createEnveloper() {
        return new EnveloperFactory().create();
    }

    public static Enveloper createEnveloperWithEvents(final Class<?>... events) {
        return new EnveloperFactory().createWithEvents(events);
    }

    public Enveloper create() {
        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
        final ObjectToJsonValueConverter converter = new ObjectToJsonValueConverter(objectMapper);
        return new Enveloper(new UtcClock(), converter);
    }

    public Enveloper createWithEvents(final Class<?>... events) {
        final Enveloper enveloper = create();

        stream(events).forEach(eventClass -> {
            if (eventClass.isAnnotationPresent(Event.class)) {
                final Event eventClassAnnotation = eventClass.getAnnotation(Event.class);
                enveloper.register(new EventFoundEvent(eventClass, eventClassAnnotation.value()));
            }
        });

        return enveloper;
    }
}
