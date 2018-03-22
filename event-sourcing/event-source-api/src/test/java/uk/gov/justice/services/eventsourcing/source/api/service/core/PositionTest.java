package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.first;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.head;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.position;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class PositionTest {

    @Test
    public void shouldBeHeadPosition() throws Exception {
        final Position position = head();
        assertThat(position.isHead(), is(true));
        assertThat(position.isFirst(), is(false));
    }

    @Test
    public void shouldBeFirstPosition() throws Exception {
        final Position position = first();
        assertThat(position.isFirst(), is(true));
        assertThat(position.isHead(), is(false));
    }

    @Test
    public void shouldReturnFirstForSequenceOne() throws Exception {
        final Position position = position(1L);
        assertThat(position.isFirst(), is(true));
        assertThat(position.isHead(), is(false));
    }

    @Test
    public void shouldReturnSequence() throws Exception {
        final Position position = position(3L);
        assertThat(position.getPosition(), is(3L));
        assertThat(position.isFirst(), is(false));
        assertThat(position.isHead(), is(false));
    }

    @Test
    public void shouldBeValidEquality() throws Exception {
        new EqualsTester()
                .addEqualityGroup(head(), head())
                .addEqualityGroup(first())
                .addEqualityGroup(position(4L), position(4L))
                .addEqualityGroup(position(5L))
                .testEquals();
    }
}