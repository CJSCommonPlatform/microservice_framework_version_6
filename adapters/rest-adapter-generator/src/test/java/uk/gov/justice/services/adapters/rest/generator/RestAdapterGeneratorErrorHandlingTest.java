package uk.gov.justice.services.adapters.rest.generator;


import static java.util.Collections.emptyMap;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.adapters.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;

import uk.gov.justice.raml.common.validator.RamlValidationException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class RestAdapterGeneratorErrorHandlingTest {
    private static final String BASE_PACKAGE = "uk.test";
    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private RestAdapterGenerator generator = new RestAdapterGenerator();

    @Test
    public void shouldThrowExceptionIfNoResourcesInRaml() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("No resources specified");

        generator.run(
                raml().build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

    }

    @Test
    public void shouldThrowExceptionIfNoActionsInRaml() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("No actions to process");

        generator.run(
                raml()
                        .with(resource("/path"))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test
    public void shouldThrowExceptionIfRequestTypeNotSetForPOSTAction() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Request type not set");

        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(httpAction(POST))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }


    @Test
    public void shouldThrowExceptionIfNotvalidRequestType() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: nd.people.unknown.command1+json");

        generator.run(
                raml()
                        .with(resource()
                                .with(httpAction(POST, "nd.people.unknown.command1+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

    }

    @Test
    public void shouldThrowExceptionIfOneOfRequestTypesNotValid() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: application/vnd.somemediatype");

        generator.run(
                raml()
                        .with(resource()
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaType("application/vnd.somemediatype")
                                        .withMediaType("application/vnd.somemediatype+json")
                                ))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

    }

    @Test
    public void shouldThrowExceptionIfResponseTypeNotSetForGETAction() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Response type not set");

        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(httpAction(GET))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

}
