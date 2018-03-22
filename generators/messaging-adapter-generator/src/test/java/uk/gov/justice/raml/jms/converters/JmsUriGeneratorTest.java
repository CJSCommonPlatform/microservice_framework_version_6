package uk.gov.justice.raml.jms.converters;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JmsUriGeneratorTest {

    @InjectMocks
    private JmsUriGenerator jmsUriGenerator;

    @Test
    public void shouldCreateAJmsUriFromAResourceUri() throws Exception {

        final String resourceUri = "/people.event";
        assertThat(jmsUriGenerator.createJmsUriFrom(resourceUri), is("jms:topic:people.event"));
    }
}
