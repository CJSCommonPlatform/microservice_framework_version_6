package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.time.ZonedDateTime.now;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class EventTest {

    private final static UUID ID = UUID.randomUUID();
    private final static UUID STREAM_ID = UUID.randomUUID();
    private final static Long SEQUENCE_ID = 1L;
    private final static String NAME = "Test Name";
    private final static String PAYLOAD_JSON = "{\"field\": \"Value\"}";
    private final static String METADATA_JSON = "{\"field\": \"Value\"}";
    private final static ZonedDateTime TIMESTAMP = now();


    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Test
    public void equalsAndHashCode() {
        Event item1 = createEvent(ID, NAME, STREAM_ID, SEQUENCE_ID, PAYLOAD_JSON, METADATA_JSON, TIMESTAMP);
        Event item2 = createEvent(ID, NAME, STREAM_ID, SEQUENCE_ID, PAYLOAD_JSON, METADATA_JSON, TIMESTAMP);
        Event item3 = createEvent(UUID.randomUUID(), NAME, STREAM_ID, SEQUENCE_ID, PAYLOAD_JSON, METADATA_JSON, TIMESTAMP);
        Event item4 = createEvent(ID, "Different", STREAM_ID, SEQUENCE_ID, PAYLOAD_JSON, METADATA_JSON, TIMESTAMP);
        Event item5 = createEvent(ID, NAME, UUID.randomUUID(), SEQUENCE_ID, PAYLOAD_JSON, METADATA_JSON, TIMESTAMP);
        Event item6 = createEvent(ID, NAME, STREAM_ID, 5L, PAYLOAD_JSON, METADATA_JSON, TIMESTAMP);
        Event item7 = createEvent(ID, NAME, STREAM_ID, SEQUENCE_ID, "", METADATA_JSON, TIMESTAMP);
        Event item8 = createEvent(ID, NAME, STREAM_ID, SEQUENCE_ID, PAYLOAD_JSON, "", TIMESTAMP);
        Event item9 = createEvent(ID, NAME, STREAM_ID, SEQUENCE_ID, PAYLOAD_JSON, METADATA_JSON, now().minusDays(1));

        new EqualsTester()
                .addEqualityGroup(item1, item2)
                .addEqualityGroup(item3)
                .addEqualityGroup(item4)
                .addEqualityGroup(item5)
                .addEqualityGroup(item6)
                .addEqualityGroup(item7)
                .addEqualityGroup(item8)
                .addEqualityGroup(item9)
                .testEquals();
    }

    private Event createEvent(final UUID id, final String name, final UUID streamId, final long sequenceId, final String payloadJSON, final String metadataJSON, final ZonedDateTime timestamp) {
        return new Event(id, streamId, sequenceId, name, metadataJSON, payloadJSON, timestamp);
    }

}
