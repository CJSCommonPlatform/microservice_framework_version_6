package uk.gov.justice.services.generators.commons.validator;

import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RequestContentTypeRamlValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldPassIfMediaTypeContainsAValidCommand() throws Exception {

        new RequestContentTypeRamlValidator().validate(
                raml()
                        .with(resource()
                                .with(httpAction(POST, "application/vnd.command1+json")))
                        .build());
    }

    @Test
    public void shouldIgnoreInvalidMediaTypesInNonPOSTActions() throws Exception {

        new RequestContentTypeRamlValidator().validate(
                raml()
                        .with(resource()
                                .with(httpAction(GET, "application/vnd.command1+json"))
                                .with(httpAction(POST, "application/vnd.command2+json"))
                                .with(httpAction(HEAD, "application/vnd.command3+json"))
                                .with(httpAction(PUT, "application/vnd.command4+json"))
                                .with(httpAction(OPTIONS, "application/vnd.command5+json"))
                        )
                        .build());
    }
}
