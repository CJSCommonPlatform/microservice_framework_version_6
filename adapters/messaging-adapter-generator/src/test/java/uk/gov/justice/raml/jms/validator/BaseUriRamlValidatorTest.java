package uk.gov.justice.raml.jms.validator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.gov.justice.raml.common.validator.RamlValidationException;
import uk.gov.justice.raml.common.validator.RamlValidator;

import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;

public class BaseUriRamlValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private RamlValidator validator = new BaseUriRamlValidator();

    @Test
    public void shouldThrowExceptionIfBaseUriNotSetForEventListener() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Base uri not set");

        validator.validate(raml().withBaseUri(null).build());
    }

    @Test
    public void shouldThrowExceptionIfBaseUriNotvalid() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid base uri: message://INVALID/handler/message/service1");

        validator.validate(raml().withBaseUri("message://INVALID/handler/message/service1").build());
    }

    @Test
    public void shouldPassWhenCorrectBaseUri() throws Exception {

        validator.validate(raml().withBaseUri("message://command/handler/message/service1").build());
        validator.validate(raml().withBaseUri("message://event/listener/message/people").build());
    }

}
