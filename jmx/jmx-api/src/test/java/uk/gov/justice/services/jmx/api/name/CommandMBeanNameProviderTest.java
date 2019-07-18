package uk.gov.justice.services.jmx.api.name;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.management.ObjectName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommandMBeanNameProviderTest {

    @Mock
    private ObjectNameFactory objectNameFactory;

    @InjectMocks
    private CommandMBeanNameProvider commandMBeanNameProvider;

    @Test
    public void shouldCreateTheCorrectObjectNameForTheSystemCommanderMBean() throws Exception {

        final String contextName = "people";

        final ObjectName objectName = mock(ObjectName.class);

        when(objectNameFactory.create(
                "uk.gov.justice.services.framework.management",
                "type",
                "people-system-command-handler-mbean")).thenReturn(objectName);

        assertThat(commandMBeanNameProvider.create(contextName), is(objectName));

    }
}
