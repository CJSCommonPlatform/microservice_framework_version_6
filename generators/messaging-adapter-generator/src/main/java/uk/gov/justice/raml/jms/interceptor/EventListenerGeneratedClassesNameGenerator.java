package uk.gov.justice.raml.jms.interceptor;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.capitalize;

/**
 * Generates the class name for a custom EventFilterInterceptor. Given a component name of
 * 'MY_CUSTOM_EVENT_LISTENER' the resulting class name would be
 * 'MyCustomEventFilterInterceptor'
 */
public class EventListenerGeneratedClassesNameGenerator {

    /**
     * Generate a name for a custom EventFilterInterceptor
     *
     * @param eventListenerComponentName The component name from the pom. Should contain the term 'EVENT_LISTENER'
     *                                   as in 'MY_CUSTOM_EVENT_LISTENER'.
     */
    public String interceptorNameFrom(final String eventListenerComponentName, final String classNameSuffix) {

        final String choppedName = eventListenerComponentName.replace("EVENT_LISTENER", "");

        final String name = of(choppedName.split("_"))
                .map(token -> capitalize(token.toLowerCase()))
                .collect(joining());

        return name + classNameSuffix;
    }
}
