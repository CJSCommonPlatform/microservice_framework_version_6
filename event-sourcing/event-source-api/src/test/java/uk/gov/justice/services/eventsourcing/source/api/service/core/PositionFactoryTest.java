package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.FIRST;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.HEAD;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

import org.junit.Test;

public class PositionFactoryTest {

    @Test
    public void shouldCreateHead() throws Exception {

        final PositionFactory factory = new PositionFactory();
        final Position position = factory.createPosition(HEAD);
        assertThat(position.isHead(), is(true));
        assertThat(position.isFirst(), is(false));
        assertThat(position.getPosition(), is(-1L));
    }

    @Test
    public void shouldCreateFirst() throws Exception {

        final PositionFactory factory = new PositionFactory();
        final Position position = factory.createPosition(FIRST);
        assertThat(position.isHead(), is(false));
        assertThat(position.isFirst(), is(true));
        assertThat(position.getPosition(), is(1L));
    }

    @Test
    public void shouldThrowBadRequestOnInvalidSequenceId() throws Exception {
        try {
            new PositionFactory().createPosition("A");
            fail("No Exceptions thrown");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(BadRequestException.class)));
            assertThat(e.getMessage(), is("Position should be numeral, provided value: A"));
        }
    }

    @Test
    public void shouldCreateSequence() throws Exception {

        final PositionFactory factory = new PositionFactory();
        final Position position = factory.createPosition("4");
        assertThat(position.isHead(), is(false));
        assertThat(position.isFirst(), is(false));
        assertThat(position.getPosition(), is(4L));
    }
}