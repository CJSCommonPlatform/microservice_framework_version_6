package uk.gov.justice.services.adapters.rest.generator;

import static org.raml.model.ActionType.DELETE;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.TRACE;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import org.junit.Before;
import org.junit.Test;
import org.raml.model.ActionType;
import org.raml.model.Raml;

public class JaxRsInterfaceGeneratorTest {

    private JaxRsInterfaceGenerator jaxRsInterfaceGenerator;

    @Before
    public void setup() {
        jaxRsInterfaceGenerator = new JaxRsInterfaceGenerator();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfActionTypeIsDELETE() throws Exception {
        jaxRsInterfaceGenerator.generateFor(singleResourceWithActionType(DELETE));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfActionTypeIsHEAD() throws Exception {
        jaxRsInterfaceGenerator.generateFor(singleResourceWithActionType(HEAD));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfActionTypeIsOPTIONS() throws Exception {
        jaxRsInterfaceGenerator.generateFor(singleResourceWithActionType(OPTIONS));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfActionTypeIsTRACE() throws Exception {
        jaxRsInterfaceGenerator.generateFor(singleResourceWithActionType(TRACE));
    }

    private Raml singleResourceWithActionType(final ActionType actionType) {
        return raml()
                .with(resource()
                        .with(httpAction(actionType))).build();
    }
}