package uk.gov.justice.subscription.jms.core;

import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorFactory;
import uk.gov.justice.subscription.jms.parser.SubscriptionWrapper;

public class SubscriptionJmsEndpointGeneratorFactory implements GeneratorFactory<SubscriptionWrapper> {

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
                                  
