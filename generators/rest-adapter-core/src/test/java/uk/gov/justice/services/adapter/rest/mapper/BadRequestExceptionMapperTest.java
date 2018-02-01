package uk.gov.justice.services.adapter.rest.mapper;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.core.json.DefaultJsonValidationLoggerHelper;
import uk.gov.justice.services.core.json.JsonSchemaValidatonException;
import uk.gov.justice.services.core.json.JsonValidationLoggerHelper;

import javax.ws.rs.core.Response;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class BadRequestExceptionMapperTest {

    private static final String TEST_ERROR_MESSAGE = "Test Error Message.";

    @Mock
    private Logger logger;

    @Mock
    private Schema schema;

    @Spy
    private JsonValidationLoggerHelper jsonValidationLoggerHelper = new DefaultJsonValidationLoggerHelper();

    @InjectMocks
    private BadRequestExceptionMapper exceptionMapper;

    @Test
    public void shouldReturn400ResponseForBadRequestException() throws Exception {

        final Response response = exceptionMapper.toResponse(new BadRequestException(TEST_ERROR_MESSAGE));

        assertThat(response.getStatus(), is(BAD_REQUEST.getStatusCode()));
        assertThat(response.getEntity(), notNullValue());
        assertThat(response.getEntity().toString(),
                hasJsonPath("$.error", equalTo(TEST_ERROR_MESSAGE)));
    }

    @Test
    public void shouldAddJsonValidationErrorsToResponse() {
        final ValidationException validationException = new ValidationException(schema, "Test Json");
        final JsonSchemaValidatonException jsonSchemaValidatonException = new JsonSchemaValidatonException(validationException.getMessage(), validationException);
        final BadRequestException badRequestException = new BadRequestException(TEST_ERROR_MESSAGE,
                jsonSchemaValidatonException);
        final Response response = exceptionMapper.toResponse(badRequestException);
        final String body = response.getEntity().toString();
        assertThat(body, hasJsonPath("$.validationErrors.message", equalTo("#: Test Json")));
    }

}