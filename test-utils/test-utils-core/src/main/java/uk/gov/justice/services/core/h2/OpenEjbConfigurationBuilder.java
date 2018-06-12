package uk.gov.justice.services.core.h2;

import static java.lang.String.format;

import java.util.Properties;

public class OpenEjbConfigurationBuilder {

    private final Properties configuration;
    private static final String EVENT_STORE_NAME = "frameworkeventstore";
    private static final String VIEW_STORE_NAME = "frameworkviewstore";

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

    public OpenEjbConfigurationBuilder addH2EventStore() {
        return getH2Config(EVENT_STORE_NAME);
    }

    public OpenEjbConfigurationBuilder addh2ViewStore() {
        return getH2Config(VIEW_STORE_NAME);
    }

    public OpenEjbConfigurationBuilder addPostgresqlEventStore() {
        return getPostgresqlConfig(EVENT_STORE_NAME);
    }

    public OpenEjbConfigurationBuilder addPostgresqlViewStore() {
        return getPostgresqlConfig(VIEW_STORE_NAME);
    }

    public OpenEjbConfigurationBuilder addHttpEjbPort(final int port) {
        configuration.put("httpejbd.port", Integer.toString(port));
        return this;
    }

    public Properties build() {
        return configuration;
    }

    private OpenEjbConfigurationBuilder getH2Config(final String dbName) {
        configuration.put(dbName, "new://Resource?type=DataSource");
        configuration.put(format("%s.JdbcDriver", dbName), "org.h2.Driver");
        configuration.put(format("%s.JdbcUrl", dbName), "jdbc:h2:mem:test;MV_STORE=FALSE;MVCC=FALSE");
        configuration.put(format("%s.JtaManaged", dbName), "true");
        configuration.put(format("%s.UserName", dbName), "sa");
        configuration.put(format("%s.Password", dbName), "sa");
        return this;
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
