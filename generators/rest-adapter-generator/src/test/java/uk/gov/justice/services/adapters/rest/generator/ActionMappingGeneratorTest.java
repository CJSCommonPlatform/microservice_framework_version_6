package uk.gov.justice.services.adapters.rest.generator;

import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.TRACE;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raml.model.Raml;

public class ActionMappingGeneratorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfActionTypeIsHEAD() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Http Method of type HEAD is not supported by the Action Mapper");

        final Raml raml = restRamlWithDefaults()
                .with(resource("/some/path")
                        .with(httpAction(HEAD, "application/vnd.default+json"))
                ).build();

        new ActionMappingGenerator().generateFor(raml);
    }

    @Test
    public void shouldThrowExceptionIfActionTypeIsOPTIONS() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Http Method of type OPTIONS is not supported by the Action Mapper");

        final Raml raml = restRamlWithDefaults()
                .with(resource("/some/path")
                        .with(httpAction(OPTIONS, "application/vnd.default+json"))
                ).build();

        new ActionMappingGenerator().generateFor(raml);
    }

    @Test
    public void shouldThrowExceptionIfActionTypeIsTRACE() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Http Method of type TRACE is not supported by the Action Mapper");

        final Raml raml = restRamlWithDefaults()
                .with(resource("/some/path")
                        .with(httpAction(TRACE, "application/vnd.default+json"))
                ).build();

        new ActionMappingGenerator().generateFor(raml);
    }
}