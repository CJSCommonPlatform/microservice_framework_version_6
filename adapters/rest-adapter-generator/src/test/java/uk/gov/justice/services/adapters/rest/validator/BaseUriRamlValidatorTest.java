package uk.gov.justice.services.adapters.rest.validator;

import uk.gov.justice.raml.common.validator.RamlValidationException;
import uk.gov.justice.raml.common.validator.RamlValidator;
import uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BaseUriRamlValidatorTest {

    private RamlValidator validator = new BaseUriRamlValidator();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldPassIfBaseUriContainsTheQueryPillarAndApiTier() throws Exception {
        validator.validate(new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withBaseUri("http://localhost:8080/warname/query/api/rest/service")
                .build());
    }

    @Test
    public void shouldPassIfBaseUriContainsTheQueryPillarAndControllerTier() throws Exception {
        validator.validate(new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withBaseUri("http://localhost:8080/warname/query/controller/rest/service")
                .build());
    }

    @Test
    public void shouldPassIfBaseUriContainsTheQueryPillarAndViewTier() throws Exception {
        validator.validate(new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withBaseUri("http://localhost:8080/warname/query/view/rest/service")
                .build());
    }

    @Test
    public void shouldPassIfBaseUriContainsTheCommandPillarAndApiTier() throws Exception {
        validator.validate(new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withBaseUri("http://localhost:8080/warname/command/api/rest/service")
                .build());
    }

    @Test
    public void shouldPassIfBaseUriContainsTheCommandPillarAndControllerTier() throws Exception {
        validator.validate(new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withBaseUri("http://localhost:8080/warname/command/controller/rest/service")
                .build());
    }

    @Test
    public void shouldPassIfBaseUriContainsTheCommandPillarAndHandlerTier() throws Exception {
        validator.validate(new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withBaseUri("http://localhost:8080/warname/command/handler/rest/service")
                .build());
    }

    @Test
    public void shouldThrowExceptionIfIfBaseUriDoesNotContainThePillar() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Pillar and tier of service should be specified in the base uri: http://localhost:8080/warname/api/rest/service");

        validator.validate(new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withBaseUri("http://localhost:8080/warname/api/rest/service")
        .build());
    }

    @Test
    public void shouldThrowExceptionIfIfBaseUriDoesNotContainTheTier() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Pillar and tier of service should be specified in the base uri: http://localhost:8080/warname/command/rest/service");

        validator.validate(new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withBaseUri("http://localhost:8080/warname/command/rest/service")
        .build());
    }

    @Test
    public void shouldThrowExceptionIfIfBaseUriDoesNotContainCorrectPillarAndTier() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Pillar and tier of service should be specified in the base uri: http://localhost:8080/warname/event/listener/rest/service");

        validator.validate(new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withBaseUri("http://localhost:8080/warname/event/listener/rest/service")
        .build());
    }

}