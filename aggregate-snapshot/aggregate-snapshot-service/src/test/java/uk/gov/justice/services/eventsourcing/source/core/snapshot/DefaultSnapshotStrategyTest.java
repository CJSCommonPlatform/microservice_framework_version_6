package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSnapshotStrategyTest {

    @Mock
    Logger logger;

    @InjectMocks
    private DefaultSnapshotStrategy snapshotStrategy;

    @Test
    public void shouldCreateAggregateSnapshotWhenDifferenceGreaterOrEqualThanThreshold() {
        snapshotStrategy.snapshotThreshold = 25L;
        assertThat(snapshotStrategy.shouldCreateSnapshot(26L, 0L), is(true));
        assertThat(snapshotStrategy.shouldCreateSnapshot(25L, 0L), is(true));


        snapshotStrategy.snapshotThreshold = 22L;
        assertThat(snapshotStrategy.shouldCreateSnapshot(23L, 0L), is(true));
        assertThat(snapshotStrategy.shouldCreateSnapshot(22L, 0L), is(true));

    }


    @Test
    public void shouldNotCreateAggregageSnapshotWhenDifferenceIsLessThanThreshold() {

        snapshotStrategy.snapshotThreshold = 25L;
        assertThat(snapshotStrategy.shouldCreateSnapshot(24L, 0L), is(false));

        snapshotStrategy.snapshotThreshold = 22L;
        assertThat(snapshotStrategy.shouldCreateSnapshot(21L, 0L), is(false));


    }

    @Test
    public void shouldNotCreateAggregageSnapshotWhenDifferenceIsLessThanThresholdWhenSnapshotIsAlreadyAvailable() {

        long streamVersionId = 51L;
        long snapshotVersionId = 25L;
        snapshotStrategy.snapshotThreshold = 25;

        assertEquals(true, snapshotStrategy.shouldCreateSnapshot(streamVersionId, snapshotVersionId));
    }

    @Test
    public void shouldCreateAggregageSnapshotWhenDifferenceIsEqualToThresholdWhenSnapshotIsAlreadyAvailable() {

        long streamVersionId = 50L;
        long snapshotVersionId = 25L;
        snapshotStrategy.snapshotThreshold = 25;

        assertEquals(true, snapshotStrategy.shouldCreateSnapshot(streamVersionId, snapshotVersionId));
    }

    @Test
    public void shouldNotCreateAggregateSnapshotWhenDifferenceIsLessOrEqualThanThresholdWhenSnapshotIsAlreadyAvailable() {
        snapshotStrategy.snapshotThreshold = 25L;
        assertThat(snapshotStrategy.shouldCreateSnapshot(49L, 25L), is(false));
        assertThat(snapshotStrategy.shouldCreateSnapshot(74L, 50L), is(false));

        snapshotStrategy.snapshotThreshold = 20L;
        assertThat(snapshotStrategy.shouldCreateSnapshot(39L, 20L), is(false));
        assertThat(snapshotStrategy.shouldCreateSnapshot(79L, 60L), is(false));

    }
}
