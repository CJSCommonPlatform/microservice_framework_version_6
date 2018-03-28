package uk.gov.justice.raml.jms.core;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorConfig;
import uk.gov.justice.raml.jms.converters.RamlToJmsSubscriptionConverter;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.generators.commons.validator.RamlValidator;
import uk.gov.justice.subscription.domain.SubscriptionDescriptor;
import uk.gov.justice.subscription.domain.SubscriptionDescriptorDef;
import uk.gov.justice.subscription.jms.core.SubscriptionJmsEndpointGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.Raml;

@RunWith(MockitoJUnitRunner.class)
public class JmsEndpointGeneratorTest {

    @Mock
    private RamlValidator ramlValidator;

    @Mock
    private SubscriptionJmsEndpointGenerator subscriptionJmsEndpointGenerator;

    @Mock
    private RamlToJmsSubscriptionConverter ramlToJmsSubscriptionConverter;

    @InjectMocks
    private JmsEndpointGenerator jmsEndpointGenerator;

    @Test
    public void shouldConvertRamlToASubscriptionThenRunTheEndpointGenerators() throws Exception {

        final String serviceComponent = "EVENT_LISTENER";

        final Raml raml = mock(Raml.class);
        final GeneratorConfig configuration = mock(GeneratorConfig.class);

        final CommonGeneratorProperties commonGeneratorProperties = mock(CommonGeneratorProperties.class);
        final SubscriptionDescriptorDef subscriptionDescriptorDef = mock(SubscriptionDescriptorDef.class);
        final SubscriptionDescriptor subscriptionDescriptor = mock(SubscriptionDescriptor.class);

        when(configuration.getGeneratorProperties()).thenReturn(commonGeneratorProperties);
        when(commonGeneratorProperties.getServiceComponent()).thenReturn(serviceComponent);
        when(ramlToJmsSubscriptionConverter.convert(
                raml,
                serviceComponent)).thenReturn(subscriptionDescriptorDef);
        when(subscriptionDescriptorDef.getSubscriptionDescriptor()).thenReturn(subscriptionDescriptor);

        jmsEndpointGenerator.run(raml, configuration);

        final InOrder inOrder = inOrder(
                ramlValidator,
                ramlToJmsSubscriptionConverter,
                subscriptionJmsEndpointGenerator);

        inOrder.verify(ramlValidator).validate(raml);
        inOrder.verify(ramlToJmsSubscriptionConverter).convert(raml, serviceComponent);
        inOrder.verify(subscriptionJmsEndpointGenerator).run(subscriptionDescriptor, configuration);
    }
}
