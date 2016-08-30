package uk.gov.justice.services.eventsourcing.common.snapshot;

import java.util.UUID;


import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class AggregateSnapshotTest {

    private final static UUID STREAM_ID = UUID.randomUUID();
    private final static Long SEQUENCE_ID = 1L;
    private final static String NAME = "RecordingAggregate";
    private final static byte[] AGGREGATE = "Any string you want ".getBytes();
    private final static byte[] AGGREGATE_DIFFERENT = "different string ".getBytes();

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @org.junit.Test
    public void equalsAndHashCode() {
        AggregateSnapshot item1 = createAggregateSnapshot(STREAM_ID, SEQUENCE_ID, NAME, AGGREGATE);
        AggregateSnapshot item4 = createAggregateSnapshot(STREAM_ID, SEQUENCE_ID, NAME, AGGREGATE);

        AggregateSnapshot item2 = createAggregateSnapshot(STREAM_ID, 2l, "", "".getBytes());

        AggregateSnapshot item3 = createAggregateSnapshot(UUID.randomUUID(), SEQUENCE_ID, NAME, AGGREGATE);

        AggregateSnapshot item5 = createAggregateSnapshot(UUID.randomUUID(), SEQUENCE_ID, NAME, AGGREGATE_DIFFERENT);

        AggregateSnapshot item6 = createAggregateSnapshot(STREAM_ID, 5L, NAME, AGGREGATE);
        AggregateSnapshot item7 = createAggregateSnapshot(STREAM_ID, SEQUENCE_ID, "", AGGREGATE);
        AggregateSnapshot item8 = createAggregateSnapshot(STREAM_ID, SEQUENCE_ID, NAME, "".getBytes());

        new EqualsTester()
                .addEqualityGroup(item1, item4)
                .addEqualityGroup(item2)
                .addEqualityGroup(item3)
                .addEqualityGroup(item5)
                .addEqualityGroup(item6)
                .addEqualityGroup(item7)
                .addEqualityGroup(item8)
                .testEquals();
    }

    private AggregateSnapshot createAggregateSnapshot(UUID streamId, long sequenceId, String type, byte[] bytes) {
        return new AggregateSnapshot(streamId, sequenceId, type, bytes);
    }


}
