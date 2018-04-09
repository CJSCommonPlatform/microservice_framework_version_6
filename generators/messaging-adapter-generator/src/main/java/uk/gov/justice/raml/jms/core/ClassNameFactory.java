package uk.gov.justice.raml.jms.core;

import uk.gov.justice.services.generators.commons.helper.MessagingAdapterBaseUri;
import uk.gov.justice.services.generators.commons.helper.MessagingResourceUri;

import com.squareup.javapoet.ClassName;

public class ClassNameFactory {

    public static final String JMS_LISTENER = "JmsListener";
    public static final String EVENT_FILTER = "EventFilter";
    public static final String EVENT_FILTER_INTERCEPTOR = "EventFilterInterceptor";
    public static final String EVENT_LISTENER_INTERCEPTOR_CHAIN_PROVIDER = "EventListenerInterceptorChainProvider";
    public static final String EVENT_VALIDATION_INTERCEPTOR = "EventValidationInterceptor";

    private final MessagingAdapterBaseUri baseUri;
    private final MessagingResourceUri resourceUri;
    private final String basePackageName;

    public ClassNameFactory(final MessagingAdapterBaseUri baseUri,
                            final MessagingResourceUri resourceUri,
                            final String basePackageName) {
        this.baseUri = baseUri;
        this.resourceUri = resourceUri;
        this.basePackageName = basePackageName;
    }

    /**
     * Convert given URI and component to a camel cased class name
     *
     * @param classNameSuffix class name suffix identifier
     * @return Java Poet class name
     */
    public ClassName classNameFor(final String classNameSuffix) {
        final String simpleName = baseUri.toClassName() + resourceUri.toClassName() + classNameSuffix;
        return ClassName.get(basePackageName, simpleName);
    }
}