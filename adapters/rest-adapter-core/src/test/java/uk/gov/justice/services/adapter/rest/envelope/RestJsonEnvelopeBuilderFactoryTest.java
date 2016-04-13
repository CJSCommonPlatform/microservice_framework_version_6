package uk.gov.justice.services.adapter.rest.envelope;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the {@link RestEnvelopeBuilderFactory} class.
 */
public class RestJsonEnvelopeBuilderFactoryTest {

    private RestEnvelopeBuilderFactory factory;

    @Before
    public void setup() {
        factory = new RestEnvelopeBuilderFactory();
    }

    @Test
    public void shouldReturnEnvelopeBuilder() throws Exception {
        RestEnvelopeBuilder envelopeBuilder = factory.builder();

        assertThat(envelopeBuilder, not(nullValue()));
    }
}
