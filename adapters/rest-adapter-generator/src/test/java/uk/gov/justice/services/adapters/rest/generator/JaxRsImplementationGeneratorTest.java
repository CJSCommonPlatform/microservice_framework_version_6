package uk.gov.justice.services.adapters.rest.generator;

import static org.raml.model.ActionType.DELETE;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.PUT;
import static org.raml.model.ActionType.TRACE;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.raml.core.GeneratorConfig;

import java.util.Collections;
import java.util.List;

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
    public void shouldThrowExceptionIfActionTypeIsPUT() throws Exception {
        jaxRsImplementationGenerator.generateFor(singleResourceWithActionType(PUT), COMMAND_API);
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
    public void shouldThrowExceptionIfActionTypeIsPATCH() throws Exception {
        jaxRsImplementationGenerator.generateFor(singleResourceWithActionType(PATCH), COMMAND_API);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfActionTypeIsTRACE() throws Exception {
        jaxRsImplementationGenerator.generateFor(singleResourceWithActionType(TRACE), COMMAND_API);
    }

    private List<Resource> singleResourceWithActionType(final ActionType actionType) {
        return Collections.singletonList(resource().with(action(actionType)).build());
    }
}