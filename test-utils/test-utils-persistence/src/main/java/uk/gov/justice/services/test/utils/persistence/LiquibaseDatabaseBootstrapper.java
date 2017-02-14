package uk.gov.justice.services.test.utils.persistence;

import java.sql.Connection;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Utility class to bootstrap your database before running integration tests. Particularly handy
 * if using an in-memory database
 *
 * To Use:
 *
 * <pre>
 *     <blockquote>
 *          final Connection connection = ...; // Sql {@link Connection} to the database
 *          liquibaseDatabaseBootstrapper.bootstrap(
 *                  "liquibase/my-liquibase-db-changelog.xml",
 *                  connection);
 *     </blockquote>
 * </pre>
 *
 * You will need to ensure that your liquibase changelog file is on your classpath (usually by
 * adding the liquibase jars to maven)
 */
public class LiquibaseDatabaseBootstrapper {

    private static final String NO_CONTEXT = "";

    /**
     * Bootstaps your database using the liqubase scripts on the classpath
     *
     * @param liquibaseDbChangelogPath the path to the liquibase change log. This should be on the classpath
     * @param connection An Sql {@link Connection} to the database
     *
     * @throws LiquibaseException if an error occurs.
     */
    public void bootstrap(final String liquibaseDbChangelogPath, final Connection connection) throws LiquibaseException {
        final Liquibase liquibase = new Liquibase(
                liquibaseDbChangelogPath,
                new ClassLoaderResourceAccessor(),
                new JdbcConnection(connection));
        liquibase.dropAll();
        liquibase.update(NO_CONTEXT);
    }
}
