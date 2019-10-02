package uk.gov.justice.services.jmx.logging;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.log.LoggerConstants.REQUEST_DATA;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;

@RunWith(MockitoJUnitRunner.class)
public class MdcLoggerTest {

    @Mock
    private ServiceContextNameProvider serviceContextNameProvider;

    @InjectMocks
    private MdcLogger mdcLogger;

    @After
    public void clearMdc() {
        MDC.clear();
    }

    @Test
    public void shouldAddServiceContextNameToMdc() {

        assertThat(MDC.get(REQUEST_DATA), is(nullValue()));

        when(serviceContextNameProvider.getServiceContextName()).thenReturn("example-service");

        mdcLogger.addServiceContextName();

        assertThat(MDC.get(REQUEST_DATA), is("{\"serviceContext\":\"example-service\"}"));
    }

    @Test
    public void shouldClearRequestDataFromMDC() {

        MDC.put(REQUEST_DATA, "Test");
        MDC.put("Other", "Stuff");

        assertThat(MDC.get(REQUEST_DATA), is("Test"));
        assertThat(MDC.get("Other"), is("Stuff"));

        mdcLogger.clearRequestData();

        assertThat(MDC.get(REQUEST_DATA), is(nullValue()));
        assertThat(MDC.get("Other"), is("Stuff"));
    }

    @Test
    public void shouldNotAddServiceContextNameToMdcIfNotPresent() {

        assertThat(MDC.get(REQUEST_DATA), is(nullValue()));

        when(serviceContextNameProvider.getServiceContextName()).thenReturn(null);

        mdcLogger.addServiceContextName();

        assertThat(MDC.get(REQUEST_DATA), is(nullValue()));
    }

    @Test
    public void shouldWrapMdcCallsAroundRunable() {

        when(serviceContextNameProvider.getServiceContextName()).thenReturn("example-service");

        assertThat(MDC.get(REQUEST_DATA), is(nullValue()));

        final List<Boolean> hasRun = new ArrayList<>();

        mdcLogger.mdcLoggerConsumer().accept(() -> {
            assertThat(MDC.get(REQUEST_DATA), is("{\"serviceContext\":\"example-service\"}"));
            hasRun.add(true);
        });

        assertThat(MDC.get(REQUEST_DATA), is(nullValue()));

        assertThat(hasRun.size(), is(1));
        assertThat(hasRun.get(0), is(true));
    }
}
