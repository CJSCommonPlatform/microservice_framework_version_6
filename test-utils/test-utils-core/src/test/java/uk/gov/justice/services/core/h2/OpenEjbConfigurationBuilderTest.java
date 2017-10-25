package uk.gov.justice.services.core.h2;

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
    public void shouldReturnAnH2EventStoreWithInitialContext() {
        final Properties properties = OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder()
                .addH2EventStore()
                .addInitialContext()
                .build();

        assertThat(properties, allOf(
                hasEntry("eventStore", "new://Resource?type=DataSource"),
                hasEntry("eventStore.JdbcDriver", "org.h2.Driver"),
                hasEntry("eventStore.JdbcUrl", "jdbc:h2:mem:test;MV_STORE=FALSE;MVCC=FALSE"),
                hasEntry("eventStore.JtaManaged", "false"),
                hasEntry("eventStore.UserName", "sa"),
                hasEntry("eventStore.Password", "sa"),
                hasEntry("java.naming.factory.initial", "org.apache.openejb.client.LocalInitialContextFactory"))
        );
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
    public void shouldReturnAnH2ViewStoreWithInitialContext() {
        final Properties properties = OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder()
                .addh2ViewStore()
                .addInitialContext()
                .build();

        assertThat(properties, allOf(
                hasEntry("viewStore", "new://Resource?type=DataSource"),
                hasEntry("viewStore.JdbcDriver", "org.h2.Driver"),
                hasEntry("viewStore.JdbcUrl", "jdbc:h2:mem:test;MV_STORE=FALSE;MVCC=FALSE"),
                hasEntry("viewStore.JtaManaged", "false"),
                hasEntry("viewStore.UserName", "sa"),
                hasEntry("viewStore.Password", "sa"),
                hasEntry("java.naming.factory.initial", "org.apache.openejb.client.LocalInitialContextFactory"))
        );
    }

    @Test
    public void shouldReturnAnPostgresqlViewStoreWithInitialContext() {
        final Properties properties = OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder()
                .addPostgresqlViewStore()
                .addInitialContext()
                .build();

        assertThat(properties, allOf(
                hasEntry("viewStore", "new://Resource?type=DataSource"),
                hasEntry("viewStore.JdbcDriver", "org.postgresql.Driver"),
                hasEntry("viewStore.JdbcUrl", "jdbc:postgresql://localhost:5432/view-store"),
                hasEntry("viewStore.JtaManaged", "false"),
                hasEntry("viewStore.UserName", "postgres"),
                hasEntry("viewStore.Password", "1"),
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
                hasEntry("eventStore", "new://Resource?type=DataSource"),
                hasEntry("eventStore.JdbcDriver", "org.postgresql.Driver"),
                hasEntry("eventStore.JdbcUrl", "jdbc:postgresql://localhost:5432/view-store"),
                hasEntry("eventStore.JtaManaged", "false"),
                hasEntry("eventStore.UserName", "postgres"),
                hasEntry("eventStore.Password", "1"),
                hasEntry("java.naming.factory.initial", "org.apache.openejb.client.LocalInitialContextFactory"))
        );
    }


}