package uk.gov.justice.services.adapter.rest.interceptor;

import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;
import uk.gov.justice.services.messaging.exception.InvalidMediaTypeException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ReaderInterceptorContext;

import com.google.common.io.CharStreams;
import org.everit.json.schema.ValidationException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * Unit tests for the {@link JsonSchemaValidationInterceptor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonSchemaValidationInterceptorTest {

    private static final String PAYLOAD = "test payload";
    private static final String MEDIA_TYPE_TYPE = "application";
    private static final String MEDIA_SUBTYPE = "vnd.test-name+json";
    private static final MediaType MEDIA_TYPE = new MediaType(MEDIA_TYPE_TYPE, MEDIA_SUBTYPE);
    private static final uk.gov.justice.services.core.mapping.MediaType CONVERTED_MEDIA_TYPE
            = new uk.gov.justice.services.core.mapping.MediaType(MEDIA_TYPE_TYPE, MEDIA_SUBTYPE);
    private static final String NON_JSON_MEDIA_SUBTYPE = "vnd.test-name+xml";

    @Mock
    private Logger logger;

    @Mock
    private ReaderInterceptorContext context;

    @Mock
    private Object proceed = mock(Object.class);

    @Mock
    private JsonSchemaValidator jsonSchemaValidator;

    @Mock
    private NameToMediaTypeConverter nameToMediaTypeConverter;

    @InjectMocks
    private JsonSchemaValidationInterceptor jsonSchemaValidationInterceptor;

    @Before
    public void setup() throws Exception {
        when(context.getInputStream()).thenReturn(inputStream(PAYLOAD));
        when(context.getMediaType()).thenReturn(MEDIA_TYPE);
        when(context.proceed()).thenReturn(proceed);
    }

    @Test
    public void shouldReturnResultOfContextProceed() throws Exception {
        assertThat(jsonSchemaValidationInterceptor.aroundReadFrom(context), equalTo(proceed));
    }

    @Test
    public void shouldSetInputStreamToOriginalPayload() throws Exception {
        jsonSchemaValidationInterceptor.aroundReadFrom(context);
        verify(context).setInputStream(argThat(inputStreamEqualTo(PAYLOAD)));
    }

    @Test
    public void shouldValidatePayloadAgainstSchema() throws Exception {

        final String actionName = "example.action-name";

        when(nameToMediaTypeConverter.convert(CONVERTED_MEDIA_TYPE)).thenReturn(actionName);

        jsonSchemaValidationInterceptor.aroundReadFrom(context);

        verify(jsonSchemaValidator).validate(PAYLOAD, actionName, of(CONVERTED_MEDIA_TYPE));
    }

    @Test
    public void shouldSkipValidationIfNonJsonPayloadType() throws Exception {

        final String actionName = "example.action-name";

        when(nameToMediaTypeConverter.convert(CONVERTED_MEDIA_TYPE)).thenReturn(actionName);
        when(context.getMediaType()).thenReturn(new MediaType(MEDIA_TYPE_TYPE, NON_JSON_MEDIA_SUBTYPE));

        jsonSchemaValidationInterceptor.aroundReadFrom(context);
        verify(jsonSchemaValidator, never()).validate(PAYLOAD, actionName, of(CONVERTED_MEDIA_TYPE));
    }

    @Test(expected = BadRequestException.class)
    @SuppressWarnings("unchecked")
    public void shouldThrowBadRequestExceptionIfValidatorFailsWithValidationException() throws Exception {
        final MultivaluedMap<String, String> headers = new MultivaluedHashMap();
        final String actionName = "example.action-name";

        when(nameToMediaTypeConverter.convert(CONVERTED_MEDIA_TYPE)).thenReturn(actionName);

        doThrow(new ValidationException("")).when(jsonSchemaValidator).validate(PAYLOAD, actionName, of(CONVERTED_MEDIA_TYPE));
        when(context.getHeaders()).thenReturn(headers);

        jsonSchemaValidationInterceptor.aroundReadFrom(context);
    }

    @Test(expected = BadRequestException.class)
    @SuppressWarnings("unchecked")
    public void shouldThrowBadRequestExceptionIfValidatorFailsWithInvalidMediaTypeException() throws Exception {
        final MultivaluedMap<String, String> headers = new MultivaluedHashMap();
        final String actionName = "example.action-name";

        when(nameToMediaTypeConverter.convert(CONVERTED_MEDIA_TYPE)).thenReturn(actionName);

        doThrow(new InvalidMediaTypeException("", mock(Exception.class))).when(jsonSchemaValidator).validate(PAYLOAD, actionName, of(CONVERTED_MEDIA_TYPE));
        when(context.getHeaders()).thenReturn(headers);

        jsonSchemaValidationInterceptor.aroundReadFrom(context);
    }

    private InputStream inputStream(final String input) throws IOException {
        return new ByteArrayInputStream(input.getBytes("UTF-8"));
    }

    private Matcher<InputStream> inputStreamEqualTo(final String input) throws IOException {

        return new TypeSafeMatcher<InputStream>() {

            @Override
            protected boolean matchesSafely(final InputStream item) {
                try {
                    final String actual = CharStreams.toString(new InputStreamReader(item));
                    item.reset();
                    return input.equals(actual);
                } catch (IOException ex) {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(input);
            }
        };
    }
}
