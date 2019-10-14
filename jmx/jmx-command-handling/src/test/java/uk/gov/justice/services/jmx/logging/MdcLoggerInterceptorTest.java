package uk.gov.justice.services.jmx.logging;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.log.LoggerConstants.REQUEST_DATA;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;

import javax.interceptor.InvocationContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;

@RunWith(MockitoJUnitRunner.class)
public class MdcLoggerInterceptorTest {

    @Mock
    private ServiceContextNameProvider serviceContextNameProvider;

    @InjectMocks
    private MdcLoggerInterceptor mdcLoggerInterceptor;

    @Test
    public void shouldAddServiceContextNameToRequestDataMdcLogging() throws Exception {

        final InvocationContext invocationContext = mock(InvocationContext.class);

        when(serviceContextNameProvider.getServiceContextName()).thenReturn("exampleService");

        when(invocationContext.proceed()).thenAnswer(invocationOnMock -> {
            assertThat(MDC.get(REQUEST_DATA), isJson(allOf(
                    withJsonPath("$.serviceContext", equalTo("exampleService"))
            )));
            return null;
        });

        mdcLoggerInterceptor.addRequestDataToMdc(invocationContext);

        assertThat(MDC.get(REQUEST_DATA), nullValue());
    }

    @Test
    public void shouldNotAddServiceContextNameIfNull() throws Exception {

        final InvocationContext invocationContext = mock(InvocationContext.class);

        when(serviceContextNameProvider.getServiceContextName()).thenReturn(null);

        when(invocationContext.proceed()).thenAnswer(invocationOnMock -> {
            assertThat(MDC.get(REQUEST_DATA), nullValue());
            return null;
        });

        mdcLoggerInterceptor.addRequestDataToMdc(invocationContext);

        assertThat(MDC.get(REQUEST_DATA), nullValue());
    }
}