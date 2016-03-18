package uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

import java.util.UUID;

public class EventLogTest {

    private final static UUID ID = UUID.randomUUID();
    private final static UUID STREAM_ID = UUID.randomUUID();
    private final static Long SEQUENCE_ID = 1L;
    private final static String NAME = "Test Name";
    private final static String PAYLOAD_JSON = "{\"field\": \"Value\"}";
    private final static String METADATA_JSON = "{\"field\": \"Value\"}";

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Test
    public void equalsAndHashCode() {
        EventLog item1 = createEventLog(ID, NAME, STREAM_ID, SEQUENCE_ID, PAYLOAD_JSON, METADATA_JSON);
        EventLog item2 = createEventLog(ID, NAME, STREAM_ID, SEQUENCE_ID, PAYLOAD_JSON, METADATA_JSON);
        EventLog item3 = createEventLog(UUID.randomUUID(), NAME, STREAM_ID, SEQUENCE_ID, PAYLOAD_JSON, METADATA_JSON);
        EventLog item4 = createEventLog(ID, "Different", STREAM_ID, SEQUENCE_ID, PAYLOAD_JSON, METADATA_JSON);
        EventLog item5 = createEventLog(ID, NAME, UUID.randomUUID(), SEQUENCE_ID, PAYLOAD_JSON, METADATA_JSON);
        EventLog item6 = createEventLog(ID, NAME, STREAM_ID, 5L, PAYLOAD_JSON, METADATA_JSON);
        EventLog item7 = createEventLog(ID, NAME, STREAM_ID, SEQUENCE_ID, "", METADATA_JSON);
        EventLog item8 = createEventLog(ID, NAME, STREAM_ID, SEQUENCE_ID, PAYLOAD_JSON, "");

        new EqualsTester()
                .addEqualityGroup(item1, item2)
                .addEqualityGroup(item3)
                .addEqualityGroup(item4)
                .addEqualityGroup(item5)
                .addEqualityGroup(item6)
                .addEqualityGroup(item7)
                .addEqualityGroup(item8)
                .testEquals();
    }

    private EventLog createEventLog(UUID id, String name, UUID streamId, long sequenceId, String payloadJSON, String metadataJSON) {
        return new EventLog(id, streamId, sequenceId, name, metadataJSON, payloadJSON);
    }

}
