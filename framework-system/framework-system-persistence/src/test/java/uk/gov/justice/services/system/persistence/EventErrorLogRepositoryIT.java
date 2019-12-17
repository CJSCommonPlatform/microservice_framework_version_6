package uk.gov.justice.services.system.persistence;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.SystemJdbcDataSourceProvider;
import uk.gov.justice.services.system.domain.EventError;
import uk.gov.justice.services.test.utils.persistence.TestJdbcDataSourceProvider;

import java.time.ZonedDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventErrorLogRepositoryIT {

    @Mock
    private SystemJdbcDataSourceProvider systemJdbcDataSourceProvider;

    @InjectMocks
    private EventErrorLogRepository eventErrorLogRepository;

    @Test
    public void shouldStoreEventErrorLog() throws Exception {

        final DataSource systemDataSource = new TestJdbcDataSourceProvider().getSystemDataSource("framework");
        when(systemJdbcDataSourceProvider.getDataSource()).thenReturn(systemDataSource);

        eventErrorLogRepository.deleteAll();

        final ZonedDateTime now = new UtcClock().now();
        final ZonedDateTime then = now.plusSeconds(23);

        final EventError eventError_1 = new EventError(
                "messageId_1",
                "component_1",
                randomUUID(),
                "eventName_1",
                1L,
                "metadata_1",
                "payload_1",
                "errorMessage_1",
                "stacktrace_1",
                now,
                "comment_1"
        );
        final EventError eventError_2 = new EventError(
                "messageId_2",
                "component_2",
                randomUUID(),
                "eventName_2",
                2L,
                "metadata_2",
                "payload_2",
                "errorMessage_2",
                "stacktrace_2",
                then,
                "comment_2"
        );

        eventErrorLogRepository.save(eventError_1);
        eventErrorLogRepository.save(eventError_2);

        final List<EventError> eventErrors = eventErrorLogRepository.findAll();

        assertThat(eventErrors.size(), is(2));

        assertThat(eventErrors.get(0), is(eventError_1));
        assertThat(eventErrors.get(1), is(eventError_2));
    }
}
