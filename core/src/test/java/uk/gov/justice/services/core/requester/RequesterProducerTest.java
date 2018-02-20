package uk.gov.justice.services.core.requester;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherDelegate;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeInspector;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandler;
import uk.gov.justice.services.core.envelope.EnvelopeValidator;
import uk.gov.justice.services.core.envelope.MediaTypeProvider;
import uk.gov.justice.services.core.envelope.RequestResponseEnvelopeValidator;
import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;

import java.lang.reflect.Field;

import javax.enterprise.inject.spi.InjectionPoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequesterProducerTest {

    @Mock
    JsonSchemaValidator jsonSchemaValidator;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler;

    @Mock
    NameToMediaTypeConverter nameToMediaTypeConverter;

    @Mock
    MediaTypeProvider mediaTypeProvider;

    @Mock
    EnvelopeInspector envelopeInspector;

    @Mock
    DispatcherCache dispatcherCache;

    @Mock
    EnvelopePayloadTypeConverter envelopePayloadTypeConverter;

    @Mock
    SystemUserUtil systemUserUtil;

    @Mock
    JsonEnvelopeRepacker jsonEnvelopeRepacker;

    @InjectMocks
    private RequesterProducer requesterProducer;

    @Test
    public void requesterShouldDelegateAdminRequestSubstitutingUserId() throws Exception {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final Dispatcher dispatcher = mock(Dispatcher.class);

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);

        final DispatcherDelegate dispatcherDelegate = (DispatcherDelegate) requesterProducer.produceRequester(injectionPoint);

        assertThat(privateField("dispatcher", dispatcherDelegate, Dispatcher.class), is(dispatcher));


        assertThat(privateField("systemUserUtil", dispatcherDelegate, SystemUserUtil.class), is(systemUserUtil));
        assertThat(privateField("envelopePayloadTypeConverter", dispatcherDelegate, EnvelopePayloadTypeConverter.class), is(envelopePayloadTypeConverter));
        assertThat(privateField("jsonEnvelopeRepacker", dispatcherDelegate, JsonEnvelopeRepacker.class), is(jsonEnvelopeRepacker));

        final RequestResponseEnvelopeValidator requestResponseEnvelopeValidator = privateField("requestResponseEnvelopeValidator", dispatcherDelegate, RequestResponseEnvelopeValidator.class);

        assertThat(privateField("nameToMediaTypeConverter", requestResponseEnvelopeValidator, NameToMediaTypeConverter.class), is(nameToMediaTypeConverter));
        assertThat(privateField("mediaTypeProvider", requestResponseEnvelopeValidator, MediaTypeProvider.class), is(mediaTypeProvider));
        assertThat(privateField("envelopeInspector", requestResponseEnvelopeValidator, EnvelopeInspector.class), is(envelopeInspector));

        final EnvelopeValidator envelopeValidator = privateField("envelopeValidator", requestResponseEnvelopeValidator, EnvelopeValidator.class);

        assertThat(privateField("jsonSchemaValidator", envelopeValidator, JsonSchemaValidator.class), is(jsonSchemaValidator));
        assertThat(privateField("objectMapper", envelopeValidator, ObjectMapper.class), is(objectMapper));
        assertThat(privateField("envelopeValidationExceptionHandler", envelopeValidator, EnvelopeValidationExceptionHandler.class), is(envelopeValidationExceptionHandler));
    }

    @SuppressWarnings("unchecked")
    private <T> T privateField(final String fieldName, final Object object, @SuppressWarnings("unused") final Class<T> clazz) throws Exception {

        final Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }
}
