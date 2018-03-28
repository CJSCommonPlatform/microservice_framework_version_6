package uk.gov.justice.raml.jms.converters;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.Event;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.MimeType;


@RunWith(MockitoJUnitRunner.class)
public class RamlMimeTypeListToEventListConverterTest {

    @Mock
    private MimeTypeToEventConverter mimeTypeToEventConverter;

    @InjectMocks
    private RamlMimeTypeListToEventListConverter ramlMimeTypeListToEventListConverter;

    @Test
    public void shouldConvertAListOfMimeTypesToAListOfEvents() throws Exception {

        final MimeType mimeType_1 = mock(MimeType.class);
        final MimeType mimeType_2 = mock(MimeType.class);
        final MimeType mimeType_3 = mock(MimeType.class);

        final Event event_1 = mock(Event.class);
        final Event event_2 = mock(Event.class);
        final Event event_3 = mock(Event.class);

        when(mimeType_1.getType()).thenReturn("mimeType_1");
        when(mimeType_2.getType()).thenReturn("mimeType_2");
        when(mimeType_3.getType()).thenReturn("mimeType_3");

        when(mimeTypeToEventConverter.asEvent(mimeType_1)).thenReturn(event_1);
        when(mimeTypeToEventConverter.asEvent(mimeType_2)).thenReturn(event_2);
        when(mimeTypeToEventConverter.asEvent(mimeType_3)).thenReturn(event_3);

        final List<MimeType> mimeTypes = asList(mimeType_1, mimeType_2, mimeType_3);

        final List<Event> events = ramlMimeTypeListToEventListConverter.toEvents(mimeTypes);

        assertThat(events.size(), is(3));
        assertThat(events.get(0), is(event_1));
        assertThat(events.get(1), is(event_2));
        assertThat(events.get(2), is(event_3));
    }

    @Test
    public void shouldReturnAnEmptyListIfAnyOfTheMimeTypesAreAPlainApplicationJsonMimeType() throws Exception {

        final MimeType mimeType_1 = mock(MimeType.class);
        final MimeType mimeType_2 = mock(MimeType.class);
        final MimeType mimeType_3 = mock(MimeType.class);

        final Event event_1 = mock(Event.class);
        final Event event_2 = mock(Event.class);
        final Event event_3 = mock(Event.class);

        when(mimeType_1.getType()).thenReturn("mimeType_1");
        when(mimeType_2.getType()).thenReturn("mimeType_2");
        when(mimeType_3.getType()).thenReturn("application/json");

        when(mimeTypeToEventConverter.asEvent(mimeType_1)).thenReturn(event_1);
        when(mimeTypeToEventConverter.asEvent(mimeType_2)).thenReturn(event_2);
        when(mimeTypeToEventConverter.asEvent(mimeType_3)).thenReturn(event_3);

        final List<MimeType> mimeTypes = asList(mimeType_1, mimeType_2, mimeType_3);

        final List<Event> events = ramlMimeTypeListToEventListConverter.toEvents(mimeTypes);

        assertThat(events.size(), is(0));
    }
}
