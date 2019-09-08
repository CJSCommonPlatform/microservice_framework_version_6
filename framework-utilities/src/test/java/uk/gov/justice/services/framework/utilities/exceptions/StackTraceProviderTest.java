package uk.gov.justice.services.framework.utilities.exceptions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StackTraceProviderTest {

    @InjectMocks
    private StackTraceProvider stackTraceProvider;

    @Test
    public void shouldGetTheStackTraceOfAnExceptionAsAString() throws Exception {

        final String stackTrace = stackTraceProvider.getStackTrace(new Exception("Ooops"));

        final String stackTracePrefix = "java.lang.Exception: Ooops\n" +
                "\tat uk.gov.justice.services.framework.utilities.exceptions.StackTraceProviderTest.shouldGetTheStackTraceOfAnExceptionAsAString(StackTraceProviderTest.java";

        assertThat(stackTrace.startsWith(stackTracePrefix), is(true));
    }
}
