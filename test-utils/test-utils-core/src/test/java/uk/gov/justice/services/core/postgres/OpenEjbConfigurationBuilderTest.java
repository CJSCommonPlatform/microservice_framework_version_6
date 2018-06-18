package uk.gov.justice.services.core.postgres;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class OpenEjbConfigurationBuilderTest {


    @Test
    public void shouldReturnAnEmptyProperties() {
        final Properties properties = OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder().build();
        Assert.assertNotNull(properties);
    }

    @Test
    public void shouldAddAnHttpEjbPort() {
        final Properties properties = OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder()
                .addHttpEjbPort(-1)
                .build();

        assertThat(properties, allOf(
                hasEntry("httpejbd.port", "-1"))
        );
    }

    @Test
    public void shouldReturnAnPostgresqlViewStoreWithInitialContext() {
        final Properties properties = OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder()
                .addPostgresqlViewStore()
                .addInitialContext()
                .build();

        assertThat(properties, allOf(
                hasEntry("frameworkviewstore", "new://Resource?type=DataSource"),
                hasEntry("frameworkviewstore.JdbcDriver", "org.postgresql.Driver"),
                hasEntry("frameworkviewstore.JdbcUrl", "jdbc:postgresql://localhost:5432/frameworkviewstore"),
                hasEntry("frameworkviewstore.JtaManaged", "false"),
                hasEntry("frameworkviewstore.UserName", "framework"),
                hasEntry("frameworkviewstore.Password", "framework"),
                hasEntry("java.naming.factory.initial", "org.apache.openejb.client.LocalInitialContextFactory"))
        );
    }

    @Test
    public void shouldReturnAnPostgresqlEventStoreWithInitialContext() {
        final Properties properties = OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder()
                .addPostgresqlEventStore()
                .addInitialContext()
                .build();

        assertThat(properties, allOf(
                hasEntry("frameworkeventstore", "new://Resource?type=DataSource"),
                hasEntry("frameworkeventstore.JdbcDriver", "org.postgresql.Driver"),
                hasEntry("frameworkeventstore.JdbcUrl", "jdbc:postgresql://localhost:5432/frameworkeventstore"),
                hasEntry("frameworkeventstore.JtaManaged", "false"),
                hasEntry("frameworkeventstore.UserName", "framework"),
                hasEntry("frameworkeventstore.Password", "framework"),
                hasEntry("java.naming.factory.initial", "org.apache.openejb.client.LocalInitialContextFactory"))
        );
    }


}
