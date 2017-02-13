package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.domain.aggregate.NoSerializableTestAggregate;
import uk.gov.justice.domain.aggregate.TestAggregate;
import uk.gov.justice.domain.snapshot.AggregateSnapshot;
import uk.gov.justice.domain.snapshot.VersionedAggregate;
import uk.gov.justice.services.core.aggregate.exception.AggregateChangeDetectedException;
import uk.gov.justice.services.eventsourcing.jdbc.snapshot.jdbc.snapshot.SnapshotRepository;

import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSnapshotServiceTest {

    private static final UUID STREAM_ID = randomUUID();

    @Mock
    private SnapshotRepository snapshotRepository;

    @Mock
    private SnapshotStrategy snapshotStrategy;

    @Mock
    private Logger logger;

    @Captor
    private ArgumentCaptor<AggregateSnapshot<TestAggregate>> snapshotArgumentCaptor;

    @InjectMocks
    private DefaultSnapshotService snapshotService;


    @Test
    public void shouldCreateAggregateWhenLatestVersionRequested() throws AggregateChangeDetectedException {
        final Optional<AggregateSnapshot<TestAggregate>> aggregateSnapshot = Optional.empty();
        when(snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class)).thenReturn(aggregateSnapshot);

        final Optional<VersionedAggregate<TestAggregate>> versionedAggregate = snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class);

        assertThat(versionedAggregate, notNullValue());
        assertThat(versionedAggregate.isPresent(), is(false));
    }

    @Test
    public void shouldRemoveAllSnapshots() throws AggregateChangeDetectedException {

        snapshotService.removeAllSnapshots(STREAM_ID, TestAggregate.class);
        verify(snapshotRepository).removeAllSnapshots(STREAM_ID, TestAggregate.class);
    }

    @Test
    public void shouldCreateSnapshotIfStrategyMandatesCreation() throws AggregateChangeDetectedException {
        final TestAggregate aggregate = new TestAggregate();
        final Optional<AggregateSnapshot<TestAggregate>> aggregateSnapshot = Optional.empty();
        final Long currentSnapshotVersion = 0l;
        final Long currentAggregateVersionId = 26l;
        when(snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class)).thenReturn(aggregateSnapshot);
        when(snapshotRepository.getLatestSnapshotVersion(STREAM_ID, TestAggregate.class)).thenReturn(currentSnapshotVersion);
        when(snapshotStrategy.shouldCreateSnapshot(currentAggregateVersionId, currentSnapshotVersion)).thenReturn(true);

        snapshotService.attemptAggregateStore(STREAM_ID, currentAggregateVersionId, aggregate);

        verify(snapshotRepository, times(1)).storeSnapshot(snapshotArgumentCaptor.capture());

        assertThat(snapshotArgumentCaptor.getValue(), notNullValue());
        assertThat(snapshotArgumentCaptor.getValue().getVersionId(), is(currentAggregateVersionId));

    }

    @Test
    public void shouldNotCreateAggregateIfStrategyDoesNotMandatesCreation() throws AggregateChangeDetectedException {
        final Long currentSnapshotVersion = 0l;
        final Long currentAggregateVersionId = 26l;
        final TestAggregate aggregate = new TestAggregate();
        when(snapshotRepository.getLatestSnapshotVersion(STREAM_ID, TestAggregate.class)).thenReturn(currentSnapshotVersion);
        when(snapshotStrategy.shouldCreateSnapshot(currentAggregateVersionId, currentSnapshotVersion)).thenReturn(false);

        snapshotService.attemptAggregateStore(STREAM_ID, currentAggregateVersionId, aggregate);
        verify(snapshotRepository, never()).storeSnapshot(any(AggregateSnapshot.class));


    }


    @Test
    public void shouldNotCreateSnapshotWhenStrategyMandatesCreationButFailsSerialization() throws AggregateChangeDetectedException {
        final NoSerializableTestAggregate aggregate = new NoSerializableTestAggregate();
        final Long currentSnapshotVersion = 16l;
        final Long currentAggregateVersionId = 36l;
        when(snapshotRepository.getLatestSnapshotVersion(STREAM_ID, NoSerializableTestAggregate.class)).thenReturn(currentSnapshotVersion);
        when(snapshotStrategy.shouldCreateSnapshot(currentAggregateVersionId, currentSnapshotVersion)).thenReturn(true);

        snapshotService.attemptAggregateStore(STREAM_ID, currentAggregateVersionId, aggregate);

        verify(snapshotRepository, never()).storeSnapshot(any(AggregateSnapshot.class));
    }

}
