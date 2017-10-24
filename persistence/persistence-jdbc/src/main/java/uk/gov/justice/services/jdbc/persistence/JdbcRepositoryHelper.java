package uk.gov.justice.services.jdbc.persistence;

import static uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper.valueOf;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.sql.DataSource;

/**
 *
 * Provides methods for returning result sets as streams
 *
 */
public class JdbcRepositoryHelper {

    public PreparedStatementWrapper preparedStatementWrapperOf(final DataSource dataSource, final String query) throws SQLException {
        return valueOf(dataSource.getConnection(), query);
    }

    public <T> Stream<T> streamOf(final PreparedStatementWrapper psWrapper, final Function<ResultSet, T> function) throws SQLException {

        final ResultSet resultSet = psWrapper.executeQuery();

        return internalStreamOf(psWrapper, resultSet, function);
    }

    /**
     * @param psWrapper               prepared statement wrapper
     * @param resultSet               jdbc resultSet
     * @param resultSetToEntityMapper - function mapping resultSet to entity of type U
     * @param <U>                     - generic type of entity
     * @return stream of entities
     */
    public <U> Stream<U> spliteratorStreamOf(final PreparedStatementWrapper psWrapper, final ResultSet resultSet, final Function<ResultSet, U> resultSetToEntityMapper) {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<U>(
                Long.MAX_VALUE, Spliterator.ORDERED) {
            @Override
            public boolean tryAdvance(final Consumer<? super U> action) {
                try {
                    if (!resultSet.next()) {
                        return false;
                    }
                    action.accept(resultSetToEntityMapper.apply(resultSet));
                    return true;
                } catch (final SQLException ex) {
                    throw handled(ex, psWrapper);
                }
            }
        }, false).onClose(psWrapper::close);
    }

    private <T> Stream<T> internalStreamOf(final PreparedStatementWrapper psWrapper, final ResultSet resultSet, final Function<ResultSet, T> function) {
        return spliteratorStreamOf(psWrapper, resultSet, e -> {
            try {
                return function.apply(e);
            } catch (final Exception ex) {
                throw handled(ex, psWrapper);
            }
        });
    }

    public JdbcRepositoryException handled(final Exception ex, final PreparedStatementWrapper psWrapper) {
        psWrapper.close();
        return new JdbcRepositoryException(ex);
    }
}
