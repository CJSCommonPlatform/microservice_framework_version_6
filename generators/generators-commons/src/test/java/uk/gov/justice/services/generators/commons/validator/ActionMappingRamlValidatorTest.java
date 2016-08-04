package uk.gov.justice.services.generators.commons.validator;


import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ActionMappingRamlValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private RamlValidator validator = new ActionMappingRamlValidator();

    @Test
    public void shouldThrowExceptionIfMediaTypeOfPOSTRequestNotInMapping() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid RAML file. Media type(s) not mapped to an action: [application/vnd.somemediatype2+json]");

        validator.validate(
                restRamlWithDefaults()
                        .with(resource("/case")
                                .with(httpAction(POST)
                                        .with(mapping()
                                                .withName("context.someAction")
                                                .withRequestType("application/vnd.somemediatype1+json"))
                                        .withMediaType("application/vnd.somemediatype1+json", "json/schema/somemediatype1.json")
                                        .withMediaType("application/vnd.somemediatype2+json", "json/schema/somemediatype2.json")
                                )

                        ).build());
    }

    @Test
    public void shouldThrowExceptionIfMediaTypeOfGETRequestNotInMapping() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid RAML file. Media type(s) not mapped to an action: [application/vnd.somemediatype1+json]");

        validator.validate(
                restRamlWithDefaults()
                        .with(resource("/case")
                                .with(httpAction(GET)
                                        .with(mapping()
                                                .withName("context.someAction")
                                                .withResponseType("application/vnd.somemediatype2+json"))
                                        .withResponseTypes(
                                                "application/vnd.somemediatype1+json",
                                                "application/vnd.somemediatype2+json"))
                        ).build());
    }
}