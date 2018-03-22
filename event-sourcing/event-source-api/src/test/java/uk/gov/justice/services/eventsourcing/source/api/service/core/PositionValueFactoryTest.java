package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.FIRST;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.HEAD;

import org.junit.Test;

public class PositionValueFactoryTest {

    @Test
    public void shouldReturnPositionValueForHead() throws Exception {

        final PositionValueFactory positionValueFactory = new PositionValueFactory();
        assertThat(positionValueFactory.getPositionValue(Position.head()), is(HEAD));
    }

    @Test
    public void shouldReturnPositionValueForFirst() throws Exception {

        final PositionValueFactory positionValueFactory = new PositionValueFactory();
        assertThat(positionValueFactory.getPositionValue(Position.first()), is(FIRST));
    }

    @Test
    public void shouldReturnPositionValueForSequence() throws Exception {

        final PositionValueFactory positionValueFactory = new PositionValueFactory();
        assertThat(positionValueFactory.getPositionValue(Position.position(3L)), is("3"));
    }
}