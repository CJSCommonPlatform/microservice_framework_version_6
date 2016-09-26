package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.domain.snapshot.AggregateChangeDetectedException;
import uk.gov.justice.domain.snapshot.AggregateSnapshot;
import uk.gov.justice.domain.snapshot.DefaultObjectInputStreamStrategy;
import uk.gov.justice.services.eventsourcing.repository.core.SnapshotRepository;
import uk.gov.justice.services.eventsourcing.source.core.exception.AggregateCreationException;

import java.util.Optional;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSnapshotServiceTest {
    private static final String ACCESS_ERROR = "Unable to create aggregate due to access error";

    private static final String CONSTRUCTION_ERROR = "Unable to create aggregate due to non instantiable class";

    private static final UUID STREAM_ID = randomUUID();
    private static final long INITIAL_AGGREGATE_VERSION = 0;

    @Mock
    SnapshotRepository snapshotRepository;

    @Mock
    SnapshotStrategy snapshotStrategy;

    @Mock
    Logger logger;

    @Captor
    ArgumentCaptor<AggregateSnapshot<TestAggregate>> snapshotArgumentCaptor;

    @InjectMocks
    private DefaultSnapshotService snapshotService;

    private final DefaultObjectInputStreamStrategy streamStrategy = new DefaultObjectInputStreamStrategy();

    @Test
    public void shouldCreateAggregate() throws AggregateChangeDetectedException {
        final Optional<AggregateSnapshot<TestAggregate>> aggregateSnapshot = Optional.empty();
        when(snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class)).thenReturn(aggregateSnapshot);

        VersionedAggregate<TestAggregate> versionedAggregate = snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class, streamStrategy);

        assertThat(versionedAggregate, notNullValue());
        assertThat(versionedAggregate.getVersionId(), is(INITIAL_AGGREGATE_VERSION));
        assertThat(versionedAggregate.getAggregate().state, nullValue());
    }

    @Test
    public void shouldCreateSnapshotIfStrategyMandatesCreation() throws AggregateChangeDetectedException {
        TestAggregate aggregate = new TestAggregate("someState");
        final Optional<AggregateSnapshot<TestAggregate>> aggregateSnapshot = Optional.empty();
        final Long initialAggregateVersionId = 0l;
        final Long currentAggregateVersionId = 26l;
        when(snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class)).thenReturn(aggregateSnapshot);
        when(snapshotStrategy.shouldCreateSnapshot(currentAggregateVersionId, initialAggregateVersionId)).thenReturn(true);

        snapshotService.attemptAggregateStore(STREAM_ID, currentAggregateVersionId, aggregate, initialAggregateVersionId);

        verify(snapshotRepository, times(1)).storeSnapshot(snapshotArgumentCaptor.capture());

        assertThat(snapshotArgumentCaptor.getValue(), notNullValue());
        assertThat(snapshotArgumentCaptor.getValue().getVersionId(), is(currentAggregateVersionId));
        assertThat(snapshotArgumentCaptor.getValue().getAggregate(streamStrategy).state, is("someState"));
    }

    @Test
    public void shouldNotCreateAggregateIfStrategyDoesNotMandatesCreation() throws AggregateChangeDetectedException {
        final Long initialAggregateVersionId = 0l;
        final Long currentAggregateVersionId = 26l;

        TestAggregate aggregate = new TestAggregate(null);
        when(snapshotStrategy.shouldCreateSnapshot(currentAggregateVersionId, initialAggregateVersionId)).thenReturn(false);

        snapshotService.attemptAggregateStore(STREAM_ID, currentAggregateVersionId, aggregate, initialAggregateVersionId);

        verifyZeroInteractions(snapshotRepository);
    }

    @Test
    public void shouldNotCreateSnapshotWhenStrategyMandatesCreationButFailsSerialization() throws AggregateChangeDetectedException {
        NoSerializableTestAggregate aggregate = new NoSerializableTestAggregate();
        final Long initialAggregateVersionId = 0l;
        final Long currentAggregateVersionId = 26l;

        when(snapshotStrategy.shouldCreateSnapshot(currentAggregateVersionId, initialAggregateVersionId)).thenReturn(true);

        snapshotService.attemptAggregateStore(STREAM_ID, currentAggregateVersionId, aggregate, initialAggregateVersionId);

        verifyZeroInteractions(snapshotRepository);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionWhenInstantiationOfAggregateFailsDuetToAccess() throws AggregateChangeDetectedException {
        expectedException.expect(AggregateCreationException.class);
        expectedException.expectMessage(ACCESS_ERROR);

        final Optional<AggregateSnapshot<IllegalAccessTestAggregate>> aggregateSnapshot = Optional.empty();
        when(snapshotRepository.getLatestSnapshot(STREAM_ID, IllegalAccessTestAggregate.class)).thenReturn(aggregateSnapshot);

        snapshotService.getLatestVersionedAggregate(STREAM_ID, IllegalAccessTestAggregate.class, streamStrategy);
    }

    @Test
    public void shouldThrowWhenInstantiationOfAggregateFails() throws AggregateChangeDetectedException {
        expectedException.expect(AggregateCreationException.class);
        expectedException.expectMessage(CONSTRUCTION_ERROR);

        final Optional<AggregateSnapshot<NonInstantiatableTestAggregate>> aggregateSnapshot = Optional.empty();
        when(snapshotRepository.getLatestSnapshot(STREAM_ID, NonInstantiatableTestAggregate.class)).thenReturn(aggregateSnapshot);

        snapshotService.getLatestVersionedAggregate(STREAM_ID, NonInstantiatableTestAggregate.class, streamStrategy);
    }


}
