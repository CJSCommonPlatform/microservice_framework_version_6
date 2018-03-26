package uk.gov.justice.raml.jms.converters;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventNameExtractorTest {

    @InjectMocks
    private EventNameExtractor eventNameExtractor;

    @Test
    public void shouldextractAnEventNameFromAMimeType() throws Exception {

        final String mimeType = "application/vnd.people.events.person-registered+json";

        assertThat(eventNameExtractor.extractEventName(mimeType), is("people.events.person-registered"));
    }
}
