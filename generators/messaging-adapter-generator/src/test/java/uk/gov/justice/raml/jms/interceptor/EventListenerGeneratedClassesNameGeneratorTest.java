package uk.gov.justice.raml.jms.interceptor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventListenerGeneratedClassesNameGeneratorTest {

    @InjectMocks
    private EventListenerGeneratedClassesNameGenerator eventListenerGeneratedClassesNameGenerator;

    @Test
    public void shouldGenerateAnInterceptorNameFromAComponentNameWithEventListenerAtTheEnd() throws Exception {

        final String classNameSuffix = "EventFilterInterceptor";
        final String eventListenerComponentName = "MY_CUSTOM_EVENT_LISTENER";

        final String className = eventListenerGeneratedClassesNameGenerator.interceptorNameFrom(
                eventListenerComponentName,
                classNameSuffix);

        assertThat(className, is("MyCustomEventFilterInterceptor"));
    }

    @Test
    public void shouldGenerateAnInterceptorNameFromAComponentNameWithEventListenerAtTheStart() throws Exception {

        final String classNameSuffix = "EventFilterInterceptor";
        final String eventListenerComponentName = "EVENT_LISTENER_CUSTOM";

        final String className = eventListenerGeneratedClassesNameGenerator.interceptorNameFrom(
                eventListenerComponentName,
                classNameSuffix);

        assertThat(className, is("CustomEventFilterInterceptor"));
    }

    @Test
    public void shouldGenerateAnInterceptorNameFromAComponentNameWithEventListenerInTheMiddle() throws Exception {

        final String classNameSuffix = "EventFilterInterceptor";
        final String eventListenerComponentName = "MY_CUSTOM_EVENT_LISTENER_NAME";

        final String className = eventListenerGeneratedClassesNameGenerator.interceptorNameFrom(
                eventListenerComponentName,
                classNameSuffix);

        assertThat(className, is("MyCustomNameEventFilterInterceptor"));
    }
}
