package uk.gov.justice.services.core.sender;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherDelegate;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeInspector;
import uk.gov.justice.services.core.envelope.EnvelopeValidator;
import uk.gov.justice.services.core.envelope.MediaTypeProvider;
import uk.gov.justice.services.core.envelope.RequestResponseEnvelopeValidator;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;

import java.lang.reflect.Field;

import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SenderProducerTest {

    @Mock
    NameToMediaTypeConverter nameToMediaTypeConverter;

    @Mock
    MediaTypeProvider mediaTypeProvider;

    @Mock
    EnvelopeInspector envelopeInspector;

    @Mock
    private DispatcherCache dispatcherCache;

    @Mock
    private SystemUserUtil systemUserUtil;

    @InjectMocks
    private SenderProducer senderProducer;

    @Test
    public void shouldCreateANewSenderDispatcherDelegate() throws Exception {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final Dispatcher dispatcher = mock(Dispatcher.class);

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);

        final DispatcherDelegate dispatcherDelegate = (DispatcherDelegate) senderProducer.produceSender(injectionPoint);

        assertThat(privateField("dispatcher", dispatcherDelegate, Dispatcher.class), is(dispatcher));
        assertThat(privateField("systemUserUtil", dispatcherDelegate, SystemUserUtil.class), is(systemUserUtil));

        final RequestResponseEnvelopeValidator requestResponseEnvelopeValidator = privateField("requestResponseEnvelopeValidator", dispatcherDelegate, RequestResponseEnvelopeValidator.class);

        assertThat(privateField("envelopeValidator", requestResponseEnvelopeValidator, EnvelopeValidator.class), is(notNullValue()));
        assertThat(privateField("nameToMediaTypeConverter", requestResponseEnvelopeValidator, NameToMediaTypeConverter.class), is(nameToMediaTypeConverter));
        assertThat(privateField("mediaTypeProvider", requestResponseEnvelopeValidator, MediaTypeProvider.class), is(mediaTypeProvider));
        assertThat(privateField("envelopeInspector", requestResponseEnvelopeValidator, EnvelopeInspector.class), is(envelopeInspector));

    }

    @SuppressWarnings("unchecked")
    private <T> T privateField(final String fieldName, final Object object, final Class<T> clazz) throws Exception {

        final Field field = object.getClass().getDeclaredField(fieldName);

        field.setAccessible(true);

        return (T) field.get(object);
    }
}
