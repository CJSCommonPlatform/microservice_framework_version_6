package uk.gov.justice.services.jdbc.persistence;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.InjectMocks;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

@RunWith(MockitoJUnitRunner.class)
public class DataSourceJndiNameProviderTest {

    @InjectMocks
    private DataSourceJndiNameProvider dataSourceJndiNameProvider;

    @Test
    public void shouldGenerateTheDataSourceNameFromTheAppName() throws Exception {

        injectNameInto(dataSourceJndiNameProvider, "my-context-command-api");

        assertThat(dataSourceJndiNameProvider.jndiName(), is("java:/app/my-context-command-api/DS.eventstore"));

        injectNameInto(dataSourceJndiNameProvider, "my-context-command-handler");

        assertThat(dataSourceJndiNameProvider.jndiName(), is("java:/app/my-context-command-handler/DS.eventstore"));
    }

    private void injectNameInto(
            final DataSourceJndiNameProvider dataSourceJndiNameProvider,
            final String warFileName) throws Exception {

        final Field warFileNameField = dataSourceJndiNameProvider.getClass().getDeclaredField("warFileName");
        warFileNameField.setAccessible(true);
        warFileNameField.set(dataSourceJndiNameProvider, warFileName);
    }
}
