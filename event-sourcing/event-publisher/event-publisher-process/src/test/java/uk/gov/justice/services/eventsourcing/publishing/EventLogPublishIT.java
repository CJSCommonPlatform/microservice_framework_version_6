package uk.gov.justice.services.eventsourcing.publishing;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishing.helpers.EventStoreInitializer;
import uk.gov.justice.services.eventsourcing.publishing.helpers.TestEventInserter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

public class EventLogPublishIT {

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    private final TestEventInserter testEventInserter = new TestEventInserter();

    private final UtcClock utcClock = new UtcClock();

    @Before
    public void initDatabase() throws Exception {
        new EventStoreInitializer().initializeEventStore(eventStoreDataSource);
    }

    @Test
    public void shouldUpdateThePublishQueueTableIfARowIsInsertedIntoTheEventLogTable() throws Exception {

        final UUID eventLogId = randomUUID();
        final UUID streamId = randomUUID();
        final int sequenceId = 98123674;

        final ZonedDateTime now = utcClock.now();

        final String eventName = "my-context.some-event-or-other";
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID(eventName))
                .withPayloadOf("the value", "some-property-name")
                .build();

        testEventInserter.insertIntoEventLog(eventLogId, streamId, sequenceId, now, eventName, jsonEnvelope);

        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM publish_queue");
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                final int id = resultSet.getInt("id");
                final String foundEventLogId = resultSet.getString("event_log_id");
                final ZonedDateTime dateQueued = fromSqlTimestamp(resultSet.getTimestamp("date_queued"));

                assertThat(resultSet.next(), is(false));
                assertThat(id, is(greaterThan(0)));
                assertThat(foundEventLogId, is(eventLogId.toString()));
                assertThat(dateQueued, is(notNullValue()));
            } else {
                fail();
            }
        }
    }
}
