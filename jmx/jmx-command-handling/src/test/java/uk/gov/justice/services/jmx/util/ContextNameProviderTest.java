package uk.gov.justice.services.jmx.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContextNameProviderTest {

    @Mock
    private ServiceContextNameProvider serviceContextNameProvider;

    @InjectMocks
    private ContextNameProvider contextNameProvider;

    @Test
    public void shouldGetTheNameOfTheContextAsTheAppNameUpToTheFirstDash() throws Exception {

        final String appName = "somecontext-service";

        when(serviceContextNameProvider.getServiceContextName()).thenReturn(appName);

        assertThat(contextNameProvider.getContextName(), is("somecontext"));
    }

    @Test
    public void shouldHandleContextNamesWithoutADash() throws Exception {

        final String appName = "somecontext";

        when(serviceContextNameProvider.getServiceContextName()).thenReturn(appName);

        assertThat(contextNameProvider.getContextName(), is(appName));
    }
}
