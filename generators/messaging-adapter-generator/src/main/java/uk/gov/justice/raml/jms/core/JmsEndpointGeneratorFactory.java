package uk.gov.justice.raml.jms.core;

import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorFactory;
import uk.gov.justice.subscription.jms.core.JmsEndpointGenerationObjects;

import com.google.common.annotations.VisibleForTesting;
import org.raml.model.Raml;

public class JmsEndpointGeneratorFactory implements GeneratorFactory<Raml> {

    private final JmsEndpointGenerationObjects jmsEndpointGenerationObjects;


    @SuppressWarnings("unused") // used by the raml maven plugin
    public JmsEndpointGeneratorFactory() {
        this(new JmsEndpointGenerationObjects());
    }

    @VisibleForTesting
    public JmsEndpointGeneratorFactory(final JmsEndpointGenerationObjects jmsEndpointGenerationObjects) {
        this.jmsEndpointGenerationObjects = jmsEndpointGenerationObjects;
    }

    public JmsEndpointGenerator create() {
        return jmsEndpointGenerationObjects.jmsEndpointGenerator();
    }
}
