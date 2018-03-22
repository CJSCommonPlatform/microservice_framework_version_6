package uk.gov.justice.subscription.jms.core;

import uk.gov.justice.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorFactory;

public class SubscriptionJmsEndpointGeneratorFactory implements GeneratorFactory<SubscriptionDescriptor> {

    private final JmsEndpointGenerationObjects jmsEndpointGenerationObjects;

    @SuppressWarnings("unused") // used by the raml maven plugin
    public SubscriptionJmsEndpointGeneratorFactory() {
        this(new JmsEndpointGenerationObjects());
    }

    public SubscriptionJmsEndpointGeneratorFactory(final JmsEndpointGenerationObjects jmsEndpointGenerationObjects) {
        this.jmsEndpointGenerationObjects = jmsEndpointGenerationObjects;
    }

    public SubscriptionJmsEndpointGenerator create() {
        return jmsEndpointGenerationObjects.subscriptionJmsEndpointGenerator();
    }
}
                                  
