package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import javax.naming.InitialContext;

import org.junit.Test;

public class InitialContextProducerTest {

    @Test
    public void shouldReturnInitialContext() throws Exception {
        final InitialContextProducer initialContextProducer = new InitialContextProducer();
        assertThat(initialContextProducer.getInitialContext(), is(instanceOf(InitialContext.class)));
    }
}