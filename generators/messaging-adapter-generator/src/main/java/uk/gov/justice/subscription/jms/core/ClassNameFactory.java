package uk.gov.justice.subscription.jms.core;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static uk.gov.justice.services.generators.commons.helper.Names.buildJavaFriendlyName;

import java.util.stream.Stream;

import com.squareup.javapoet.ClassName;

public class ClassNameFactory {

    public static final String JMS_LISTENER = "JmsListener";
    public static final String EVENT_FILTER = "EventFilter";
    public static final String EVENT_FILTER_INTERCEPTOR = "EventFilterInterceptor";
    public static final String EVENT_LISTENER_INTERCEPTOR_CHAIN_PROVIDER = "EventListenerInterceptorChainProvider";
    public static final String EVENT_VALIDATION_INTERCEPTOR = "EventValidationInterceptor";

    private final String basePackageName;
    private final String contextName;
    private final String componentName;
    private final String jmsUri;

    public ClassNameFactory(final String basePackageName,
                            final String contextName,
                            final String componentName,
                            final String jmsUri) {
        this.basePackageName = basePackageName;
        this.contextName = contextName;
        this.componentName = componentName;
        this.jmsUri = jmsUri;
    }

    /**
     * Convert given URI and component to a camel cased class name
     *
     * @param classNameSuffix class name suffix identifier
     * @return Java Poet class name
     */
    public ClassName classNameFor(final String classNameSuffix) {

        final String simpleName =
                buildJavaFriendlyName(contextName) +
                        componentNameToClassName(componentName) +
                        jmsUriToClassName(jmsUri) +
                        classNameSuffix;

        return ClassName.get(basePackageName, simpleName);
    }

    private String jmsUriToClassName(final String jmsUri) {

        final String queueName = jmsUri.split(":")[2];

        return buildJavaFriendlyName(queueName);
    }

    private String componentNameToClassName(final String componentName) {

        return Stream.of(componentName.split("_"))
                .map(token -> capitalize(token.toLowerCase()))
                .collect(joining());
    }
}
