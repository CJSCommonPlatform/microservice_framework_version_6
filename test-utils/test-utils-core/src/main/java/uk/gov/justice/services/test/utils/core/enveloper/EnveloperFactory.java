package uk.gov.justice.services.test.utils.core.enveloper;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for those who need to have a real Enveloper in their tests.
 *
 * To use:
 *  <pre>
 *  {@code
 *
 *      "@Spy"
 *      private Enveloper enveloper = EnveloperFactory.createEnveloper()
 *
 *      "@Mock"
 *      private MyDependency dependency;
 *
 *      "@InjectMocks"
 *      private MyClassUnderTest classUnderTest;
 * }
 * </pre>
 *
 *  Mockito will now inject the real Enveloper into your test class along with
 *  any other mocks.
 */
public class EnveloperFactory {

    public static Enveloper createEnveloper() {
        return new EnveloperFactory().create();
    }

    public Enveloper create() {

        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
        final ObjectToJsonValueConverter converter = new ObjectToJsonValueConverter(objectMapper);

        return new Enveloper(converter);
    }
}
