package uk.gov.justice.services.adapters.rest.generator;

import static java.util.Collections.emptyMap;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.TRACE;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;

import uk.gov.justice.services.generators.commons.validator.RamlValidationException;

import org.junit.Test;

public class HttpActionTypeValidationTest extends BaseRestAdapterGeneratorTest {

    @Test(expected = RamlValidationException.class)
    public void shouldThrowExceptionIfActionTypeIsHEAD() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .withBaseUri("http://localhost:8080/warname/query/api/rest/service")
                        .with(resource("/some/path")
                                .with(httpAction().withHttpActionType(HEAD))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test(expected = RamlValidationException.class)
    public void shouldThrowExceptionIfActionTypeIsOPTIONS() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .withBaseUri("http://localhost:8080/warname/query/api/rest/service")
                        .with(resource("/some/path")
                                .with(httpAction().withHttpActionType(OPTIONS))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test(expected = RamlValidationException.class)
    public void shouldThrowExceptionIfActionTypeIsTRACE() throws Exception {
        generator.run(
                restRamlWithDefaults()
                        .withBaseUri("http://localhost:8080/warname/query/api/rest/service")
                        .with(resource("/some/path")
                                .with(httpAction().withHttpActionType(TRACE))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }
}