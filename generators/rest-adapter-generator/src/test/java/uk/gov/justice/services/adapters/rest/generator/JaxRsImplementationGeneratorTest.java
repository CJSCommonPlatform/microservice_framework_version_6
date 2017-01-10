package uk.gov.justice.services.adapters.rest.generator;

import static org.raml.model.ActionType.DELETE;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.TRACE;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import uk.gov.justice.raml.core.GeneratorConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.ActionType;
import org.raml.model.Resource;

@RunWith(MockitoJUnitRunner.class)
public class JaxRsImplementationGeneratorTest {

    @Mock
    GeneratorConfig config;

    private JaxRsImplementationGenerator jaxRsImplementationGenerator;

    @Before
    public void setup() {
        jaxRsImplementationGenerator = new JaxRsImplementationGenerator(config);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfActionTypeIsDELETE() throws Exception {
        jaxRsImplementationGenerator.generateFor(singleResourceWithActionType(DELETE), COMMAND_API);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfActionTypeIsHEAD() throws Exception {
        jaxRsImplementationGenerator.generateFor(singleResourceWithActionType(HEAD), COMMAND_API);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfActionTypeIsOPTIONS() throws Exception {
        jaxRsImplementationGenerator.generateFor(singleResourceWithActionType(OPTIONS), COMMAND_API);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfActionTypeIsTRACE() throws Exception {
        jaxRsImplementationGenerator.generateFor(singleResourceWithActionType(TRACE), COMMAND_API);
    }

    private Resource singleResourceWithActionType(final ActionType actionType) {
        return resource().with(httpAction(actionType)).build();
    }
}