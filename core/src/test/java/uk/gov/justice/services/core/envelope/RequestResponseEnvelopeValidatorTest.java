package uk.gov.justice.services.core.envelope;


import static java.util.Optional.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestResponseEnvelopeValidatorTest {

    @Mock
    private EnvelopeValidator envelopeValidator;

    @Mock
    private NameToMediaTypeConverter nameToMediaTypeConverter;

    @Mock
    private MediaTypeProvider mediaTypeProvider;

    @Mock
    private EnvelopeInspector envelopeInspector;

    @InjectMocks
    private RequestResponseEnvelopeValidator requestResponseEnvelopeValidator;

    @Test
    public void shouldValidateARequestEnvelope() throws Exception {

        final String actionName = "example.action-name";

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final MediaType mediaType = mock(MediaType.class);

        when(envelopeInspector.getActionNameFor(jsonEnvelope)).thenReturn(actionName);
        when(nameToMediaTypeConverter.convert(actionName)).thenReturn(mediaType);

        requestResponseEnvelopeValidator.validateRequest(jsonEnvelope);

        verify(envelopeValidator).validate(jsonEnvelope, actionName, of(mediaType));
    }

    @Test
    public void shouldValidateAResponsetEnvelope() throws Exception {

        final String actionName = "example.action-name";

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Optional<MediaType> mediaType = of(mock(MediaType.class));

        when(envelopeInspector.getActionNameFor(jsonEnvelope)).thenReturn(actionName);
        when(mediaTypeProvider.getResponseMediaType(actionName)).thenReturn(mediaType);

        requestResponseEnvelopeValidator.validateResponse(jsonEnvelope);

        verify(envelopeValidator).validate(jsonEnvelope, actionName, mediaType);
    }
}
