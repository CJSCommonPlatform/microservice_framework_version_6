package uk.gov.justice.services.core.postgres;

import static java.lang.String.format;

import java.util.Properties;

public class OpenEjbConfigurationBuilder {

    private final Properties configuration;
    private static final String EVENT_STORE_NAME = "frameworkeventstore";
    private static final String VIEW_STORE_NAME = "frameworkviewstore";
    private static final String SYSTEM_NAME = "frameworksystem";

    private OpenEjbConfigurationBuilder() {
        configuration = new Properties();
    }

    public static OpenEjbConfigurationBuilder createOpenEjbConfigurationBuilder() {
        return new OpenEjbConfigurationBuilder();
    }

    public OpenEjbConfigurationBuilder addInitialContext() {
        configuration.put("java.naming.factory.initial", "org.apache.openejb.client.LocalInitialContextFactory");
        return this;
    }

    public OpenEjbConfigurationBuilder addPostgresqlEventStore() {
        return getPostgresqlConfig(EVENT_STORE_NAME);
    }

    public OpenEjbConfigurationBuilder addPostgresqlViewStore() {
        return getPostgresqlConfig(VIEW_STORE_NAME);
    }

    public OpenEjbConfigurationBuilder addPostgresqlSystem() {
        return getPostgresqlConfig(SYSTEM_NAME);
    }

    public OpenEjbConfigurationBuilder addHttpEjbPort(final int port) {
        configuration.put("httpejbd.port", Integer.toString(port));
        return this;
    }

    public Properties build() {
        return configuration;
    }

    private OpenEjbConfigurationBuilder getPostgresqlConfig(final String dbName) {
        configuration.put(dbName, "new://Resource?type=DataSource");
        configuration.put(format("%s.JdbcDriver", dbName), "org.postgresql.Driver");
        configuration.put(format("%s.JdbcUrl", dbName), format("jdbc:postgresql://localhost:5432/%s", dbName));
        configuration.put(format("%s.JtaManaged", dbName), "false");
        configuration.put(format("%s.UserName", dbName), "framework");
        configuration.put(format("%s.Password", dbName), "framework");
        return this;
    }
}
