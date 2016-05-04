package uk.gov.justice.services.adapters.rest.generator;

import static org.raml.model.ActionType.DELETE;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.PUT;
import static org.raml.model.ActionType.TRACE;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.raml.model.ActionType;
import org.raml.model.Resource;

public class JaxRsInterfaceGeneratorTest {

    private JaxRsInterfaceGenerator jaxRsInterfaceGenerator;

    @Before
    public void setup() {
        jaxRsInterfaceGenerator = new JaxRsInterfaceGenerator();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfActionTypeIsPUT() throws Exception {
        jaxRsInterfaceGenerator.generateFor(singleResourceWithActionType(PUT));
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
    public void shouldThrowExceptionIfActionTypeIsPATCH() throws Exception {
        jaxRsInterfaceGenerator.generateFor(singleResourceWithActionType(PATCH));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfActionTypeIsTRACE() throws Exception {
        jaxRsInterfaceGenerator.generateFor(singleResourceWithActionType(TRACE));
    }

    private List<Resource> singleResourceWithActionType(final ActionType actionType) {
        return Collections.singletonList(resource().with(action(actionType)).build());
    }
}