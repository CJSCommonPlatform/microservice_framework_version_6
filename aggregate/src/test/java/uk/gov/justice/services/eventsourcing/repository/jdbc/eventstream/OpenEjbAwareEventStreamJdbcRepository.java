package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static java.lang.String.format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated No longer used in the Framework and will be removed at a later date. Use {@link EventStreamJdbcRepository} instead
 */
@Deprecated
public class OpenEjbAwareEventStreamJdbcRepository {

    static final String SQL_EVENT_STREAM_COUNT_BY_STREAM_ID = "SELECT count(*) FROM event_stream WHERE stream_id=? ";


    private Logger logger = LoggerFactory.getLogger(OpenEjbAwareEventStreamJdbcRepository.class);

    @Inject
    EventStreamJdbcRepository eventStreamJdbcRepository;

    public int eventStreamCount(final UUID streamId) {
        logger.info("REPO DataSource [{}]", eventStreamJdbcRepository.dataSource);
        try (final Connection connection = eventStreamJdbcRepository.dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(SQL_EVENT_STREAM_COUNT_BY_STREAM_ID)) {
            preparedStatement.setObject(1, streamId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception getting count of event stream entries for ", streamId), e);
        }
    }

}
