package uk.gov.justice.services.example.cakeshop.it.helpers;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.dbcp2.BasicDataSource;

public class DatabaseManager {

    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";
    private static final TestProperties TEST_PROPERTIES = new TestProperties("test.properties");

    private DataSource CAKE_SHOP_DS;

    public DataSource initEventStoreDb() throws Exception {
        return initDatabase("db.eventstore.url", "db.eventstore.userName",
                "db.eventstore.password", "liquibase/event-store-db-changelog.xml", "liquibase/snapshot-store-db-changelog.xml");
    }

    public DataSource initFileServiceDb() throws Exception {
        return initDatabase("db.fileservice.url", "db.fileservice.userName",
                "db.fileservice.password", "liquibase/file-service-liquibase-db-changelog.xml");
    }

    public DataSource initViewStoreDb() throws Exception {
        CAKE_SHOP_DS = initDatabase("db.example.url", "db.example.userName", "db.example.password",
                "liquibase/view-store-db-changelog.xml", "liquibase/event-buffer-changelog.xml");
        return CAKE_SHOP_DS;
    }

    private static DataSource initDatabase(final String dbUrlPropertyName,
                                           final String dbUserNamePropertyName,
                                           final String dbPasswordPropertyName,
                                           final String... liquibaseChangeLogXmls) throws Exception {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(POSTGRES_DRIVER);

        dataSource.setUrl(TEST_PROPERTIES.value(dbUrlPropertyName));
        dataSource.setUsername(TEST_PROPERTIES.value(dbUserNamePropertyName));
        dataSource.setPassword(TEST_PROPERTIES.value(dbPasswordPropertyName));
        boolean dropped = false;
        final JdbcConnection jdbcConnection = new JdbcConnection(dataSource.getConnection());

        for (String liquibaseChangeLogXml : liquibaseChangeLogXmls) {
            Liquibase liquibase = new Liquibase(liquibaseChangeLogXml,
                    new ClassLoaderResourceAccessor(), jdbcConnection);
            if (!dropped) {
                liquibase.dropAll();
                dropped = true;
            }
            liquibase.update("");
        }
        return dataSource;
    }
}
