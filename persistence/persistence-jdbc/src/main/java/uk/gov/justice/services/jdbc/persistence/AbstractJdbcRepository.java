package uk.gov.justice.services.jdbc.persistence;

import static uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper.valueOf;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
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
            } catch (NamingException e) {
                throw new JdbcRepositoryException(e);
            }
        }
        return datasource;
    }

    protected PreparedStatementWrapper preparedStatementWrapperOf(final String selectByStreamId) throws SQLException {
        return valueOf(getDataSource().getConnection(), selectByStreamId);
    }

    protected Stream<T> streamOf(final PreparedStatementWrapper psWrapper) throws SQLException {

        final ResultSet resultSet = psWrapper.executeQuery();

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<T>(
                Long.MAX_VALUE, Spliterator.ORDERED) {
            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                try {
                    if (!resultSet.next()) {
                        return false;
                    }
                    action.accept(entityFrom(resultSet));
                    return true;
                } catch (SQLException ex) {
                    psWrapper.close();
                    throw new JdbcRepositoryException(ex);
                }
            }
        }, false).onClose(() -> psWrapper.close());
    }

    protected String warFileName() throws NamingException {
        return warFileName;
    }

    protected abstract String jndiName() throws NamingException;

    protected abstract T entityFrom(final ResultSet rs) throws SQLException;

}
