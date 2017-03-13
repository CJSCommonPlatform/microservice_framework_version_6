package uk.gov.justice.services.generators.commons.validator;

import static org.raml.model.ActionType.POST;
import static org.raml.model.ParamType.STRING;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.MimeTypeBuilder.multipartMimeType;
import static uk.gov.justice.services.generators.test.utils.builder.MimeTypeBuilder.multipartWithFileFormParameter;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raml.model.Raml;

public class MultipartHasFormParametersTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private RamlValidator validator = new MultipartHasFormParameters();

    @Test
    public void shouldPassIfMultipartContainsCorrectFormParameter() throws Exception {
        final Raml raml = restRamlWithDefaults()
                .with(resource("/some/path")
                        .with(httpAction()
                                .withHttpActionType(POST)
                                .withMediaTypeWithoutSchema(multipartWithFileFormParameter("photoId")))
                ).build();

        validator.validate(raml);
    }

    @Test
    public void shouldFailIfMultipartHasNoFormParameters() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Multipart form must contain form parameters");

        final Raml raml = restRamlWithDefaults()
                .with(resource("/some/path")
                        .with(httpAction()
                                .withHttpActionType(POST)
                                .withMediaTypeWithoutSchema(multipartMimeType()))
                ).build();

        validator.validate(raml);
    }

    @Test
    public void shouldFailIfMultipartHasFormParameterWithIncorrectType() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Multipart form parameter is expected to be of type FILE, instead was STRING");

        final Raml raml = restRamlWithDefaults()
                .with(resource("/some/path")
                        .with(httpAction()
                                .withHttpActionType(POST)
                                .withMediaTypeWithoutSchema(multipartMimeType()
                                        .withFormParameter("photoId", STRING, true)))
                ).build();

        validator.validate(raml);
    }

    @Test
    public void shouldPassIfNoMultipartPresent() throws Exception {
        final Raml raml = restRamlWithDefaults()
                .with(resource("/some/path")
                        .withDefaultPostAction()
                ).build();

        validator.validate(raml);
    }
}