package uk.gov.justice.services.fileservice.repository;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

public class TestDataSourceProvider implements DataSourceProvider {

    private final String url;
    private final String username;
    private final String password;
    private final String driverName;

    public TestDataSourceProvider(final String url, final String username, final String password) {
        this(url, username , password, org.postgresql.Driver.class.getName());
    }

    public TestDataSourceProvider(final String url, final String username, final String password, final String driverName) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverName = driverName;
    }

    @Override
    public DataSource getDataSource() {
        final BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(url);
        basicDataSource.setPassword(password);
        basicDataSource.setUsername(username);
        basicDataSource.setDriverClassName(driverName);

        return basicDataSource;
    }
}
