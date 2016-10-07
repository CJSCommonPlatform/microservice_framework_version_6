package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import static org.junit.Assert.assertEquals;

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
    public void shouldCreateAggregateSnapshotWhenDifferenceGreaterThanThreshold() {
        long streamVersionId = 26L;
        long snapshotVersionId = 0L;
        boolean canBeCreated = snapshotStrategy.shouldCreateSnapshot(streamVersionId, snapshotVersionId);

        assertEquals(true, canBeCreated);
    }

    @Test
    public void shouldNotCreateAggregateSnapshotWhenDifferenceIsEqualToThreshold() {

        long streamVersionId = 25L;
        long snapshotVersionId = 0L;
        boolean canBeCreated = snapshotStrategy.shouldCreateSnapshot(streamVersionId, snapshotVersionId);

        assertEquals(true, canBeCreated);
    }

    @Test
    public void shouldNotCreateAggregageSnapshotWhenDifferenceIsLessThanThreshold() {

        long streamVersionId = 24L;
        long snapshotVersionId = 0L;
        boolean canBeCreated = snapshotStrategy.shouldCreateSnapshot(streamVersionId, snapshotVersionId);

        assertEquals(false, canBeCreated);
    }

    @Test
    public void shouldNotCreateAggregageSnapshotWhenDifferenceIsLessThanThresholdWhenSnapshotIsAlreadyAvailable() {

        long streamVersionId = 51L;
        long snapshotVersionId = 25L;
        boolean canBeCreated = snapshotStrategy.shouldCreateSnapshot(streamVersionId, snapshotVersionId);

        assertEquals(true, canBeCreated);
    }

    @Test
    public void shouldCreateAggregageSnapshotWhenDifferenceIsEqualToThresholdWhenSnapshotIsAlreadyAvailable() {

        long streamVersionId = 50L;
        long snapshotVersionId = 25L;
        boolean canBeCreated = snapshotStrategy.shouldCreateSnapshot(streamVersionId, snapshotVersionId);

        assertEquals(true, canBeCreated);
    }

    @Test
    public void shouldCreateAggregageSnapshotWhenDifferenceIsLessThanThresholdWhenSnapshotIsAlreadyAvailable() {

        long streamVersionId = 49L;
        long snapshotVersionId = 25L;
        boolean canBeCreated = snapshotStrategy.shouldCreateSnapshot(streamVersionId, snapshotVersionId);

        assertEquals(false, canBeCreated);
    }
}
