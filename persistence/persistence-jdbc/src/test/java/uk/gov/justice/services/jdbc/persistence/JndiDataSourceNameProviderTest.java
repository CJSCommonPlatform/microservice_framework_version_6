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
public class JndiDataSourceNameProviderTest {

    @InjectMocks
    private JndiDataSourceNameProvider jndiDataSourceNameProvider;

    @Test
    public void shouldGenerateTheDataSourceNameFromTheAppName() throws Exception {

        injectNameInto(jndiDataSourceNameProvider, "my-context-command-api");

        assertThat(jndiDataSourceNameProvider.jndiName(), is("java:/app/my-context-command-api/DS.eventstore"));

        injectNameInto(jndiDataSourceNameProvider, "my-context-command-handler");

        assertThat(jndiDataSourceNameProvider.jndiName(), is("java:/app/my-context-command-handler/DS.eventstore"));
    }

    private void injectNameInto(
            final JndiDataSourceNameProvider jndiDataSourceNameProvider,
            final String warFileName) throws Exception {

        final Field warFileNameField = jndiDataSourceNameProvider.getClass().getDeclaredField("warFileName");
        warFileNameField.setAccessible(true);
        warFileNameField.set(jndiDataSourceNameProvider, warFileName);
    }
}
