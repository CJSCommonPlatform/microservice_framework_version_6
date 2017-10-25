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

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public abstract class AbstractJdbcRepository<T> {

    @Resource(lookup = "java:app/AppName")
    String warFileName;

    private Context initialContext;
    private DataSource datasource;

    protected Context getInitialContext() throws NamingException {
        if (initialContext == null) {
            initialContext = new InitialContext();
        }
        return initialContext;
    }

    protected DataSource getDataSource() {
        if (datasource == null) {
            try {
                datasource = (DataSource) getInitialContext().lookup(jndiName());
            } catch (final NamingException e) {
                throw new JdbcRepositoryException(e);
            }
        }
        return datasource;
    }

    protected PreparedStatementWrapper preparedStatementWrapperOf(final String query) throws SQLException {
        return valueOf(getDataSource().getConnection(), query);
    }

    protected Stream<T> streamOf(final PreparedStatementWrapper psWrapper) throws SQLException {

        final ResultSet resultSet = psWrapper.executeQuery();

        return streamOf(psWrapper, resultSet);
    }

    /**
     *
     * @param psWrapper prepared statement wrapper
     * @param resultSet jdbc resultSet
     * @param resultSetToEntityMapper - function mapping resultSet to entity of type U
     * @param <U> - generic type of entity
     * @return stream of entities
     */
    protected <U> Stream<U> streamOf(final PreparedStatementWrapper psWrapper, final ResultSet resultSet, final Function<ResultSet, U> resultSetToEntityMapper) {
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
        }, false).onClose(() -> psWrapper.close());
    }

    private Stream<T> streamOf(final PreparedStatementWrapper psWrapper, final ResultSet resultSet) {
        return streamOf(psWrapper, resultSet, e -> {
            try {
                return entityFrom(e);
            } catch (final SQLException ex) {
                throw handled(ex, psWrapper);
            }
        });
    }

    protected JdbcRepositoryException handled(final SQLException ex, final PreparedStatementWrapper psWrapper) {
        psWrapper.close();
        return new JdbcRepositoryException(ex);
    }

    protected String warFileName() throws NamingException {
        return warFileName;
    }

    protected abstract String jndiName() throws NamingException;

    protected abstract T entityFrom(final ResultSet rs) throws SQLException;

}
