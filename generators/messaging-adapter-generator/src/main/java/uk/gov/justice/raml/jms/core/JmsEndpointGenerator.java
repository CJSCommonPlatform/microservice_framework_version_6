package uk.gov.justice.raml.jms.core;

import uk.gov.justice.maven.generator.io.files.parser.core.Generator;
import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorConfig;
import uk.gov.justice.raml.jms.converters.RamlToJmsSubscriptionConverter;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.generators.commons.validator.RamlValidator;
import uk.gov.justice.subscription.domain.SubscriptionDescriptorDef;
import uk.gov.justice.subscription.jms.core.SubscriptionJmsEndpointGenerator;

import org.raml.model.Raml;

/**
 * Generates JMS endpoint classes out of RAML object
 */
public class JmsEndpointGenerator implements Generator<Raml> {

    private final RamlValidator ramlValidator;
    private final SubscriptionJmsEndpointGenerator subscriptionJmsEndpointGenerator;
    private final RamlToJmsSubscriptionConverter ramlToJmsSubscriptionConverter;


    public JmsEndpointGenerator(
            final RamlValidator ramlValidator,
            final SubscriptionJmsEndpointGenerator subscriptionJmsEndpointGenerator,
            final RamlToJmsSubscriptionConverter ramlToJmsSubscriptionConverter) {
        this.ramlValidator = ramlValidator;
        this.subscriptionJmsEndpointGenerator = subscriptionJmsEndpointGenerator;
        this.ramlToJmsSubscriptionConverter = ramlToJmsSubscriptionConverter;
    }

    /**
     * Generates JMS endpoint classes from a RAML document.
     *
     * @param raml          the RAML document
     * @param configuration contains package of generated sources, as well as source and destination
     *                      folders
     */
    @Override
    public void run(final Raml raml, final GeneratorConfig configuration) {

        ramlValidator.validate(raml);

        final CommonGeneratorProperties commonGeneratorProperties = (CommonGeneratorProperties) configuration.getGeneratorProperties();
        final SubscriptionDescriptorDef subscriptionDescriptorDef = ramlToJmsSubscriptionConverter.convert(
                raml,
                commonGeneratorProperties.getServiceComponent());

        subscriptionJmsEndpointGenerator.run(subscriptionDescriptorDef.getSubscriptionDescriptor(), configuration);
    }
}
