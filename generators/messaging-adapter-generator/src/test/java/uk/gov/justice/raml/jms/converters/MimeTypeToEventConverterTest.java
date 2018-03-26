package uk.gov.justice.raml.jms.converters;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.domain.subscriptiondescriptor.Event;
import uk.gov.justice.services.generators.commons.mapping.SchemaIdParser;
import uk.gov.justice.services.generators.commons.mapping.SchemaParsingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.MimeType;


@RunWith(MockitoJUnitRunner.class)
public class MimeTypeToEventConverterTest {

    @Mock
    private EventNameExtractor eventNameExtractor;

    @Mock
    private SchemaIdParser schemaIdParser;

    @InjectMocks
    private MimeTypeToEventConverter mimeTypeToEventConverter;

    @Test
    public void shouldConvertARamlMimeTypeToAUri() throws Exception {

        final String eventName = "something-happened";
        final String schemaUri = "http://justice.gov.uk/test/schema.json";
        final String mimeTypeString = "application/vnd.context.events.something-happened+json";

        final MimeType mimeType = mock(MimeType.class);

        when(mimeType.getType()).thenReturn(mimeTypeString);
        when(eventNameExtractor.extractEventName(mimeType.getType())).thenReturn(eventName);
        when(schemaIdParser.schemaIdFrom(mimeType)).thenReturn(of(schemaUri));

        final Event event = mimeTypeToEventConverter.asEvent(mimeType);

        assertThat(event.getName(), is(eventName));
        assertThat(event.getSchemaUri(), is(schemaUri));
    }

    @Test
    public void shouldThrowASchemaParsingExceptionIfTheSchemaIdIsNotFound() throws Exception {

        final String eventName = "something-happened";
        final String mimeTypeString = "application/vnd.context.events.something-happened+json";

        final MimeType mimeType = mock(MimeType.class);

        when(mimeType.getType()).thenReturn(mimeTypeString);
        when(eventNameExtractor.extractEventName(mimeType.getType())).thenReturn(eventName);
        when(schemaIdParser.schemaIdFrom(mimeType)).thenReturn(empty());

        try {
            mimeTypeToEventConverter.asEvent(mimeType);
            fail();
        } catch (final SchemaParsingException expected) {
            assertThat(expected.getMessage(), is("Schema for media type: application/vnd.context.events.something-happened+json has no schema id"));
        }
    }
}
