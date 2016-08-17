package uk.gov.justice.services.adapter.rest.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.json.JsonSchemaValidator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ReaderInterceptorContext;

import com.google.common.io.CharStreams;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link JsonSchemaValidationInterceptor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonSchemaValidationInterceptorTest {

    private static final String PAYLOAD = "test payload";
    private static final String NAME = "test-name";
    private static final String MEDIA_TYPE = "application";
    private static final String MEDIA_SUBTYPE = "vnd.test-name+json";
    private static final String NON_JSON_MEDIA_SUBTYPE = "vnd.test-name+xml";

    @Mock
    private ReaderInterceptorContext context = mock(ReaderInterceptorContext.class);

    @Mock
    private Object proceed = mock(Object.class);

    @Mock
    private JsonSchemaValidator validator;

    @InjectMocks
    private JsonSchemaValidationInterceptor interceptor;

    @Before
    public void setup() throws Exception {
        when(context.getInputStream()).thenReturn(inputStream(PAYLOAD));
        when(context.getMediaType()).thenReturn(new MediaType(MEDIA_TYPE, MEDIA_SUBTYPE));
        when(context.proceed()).thenReturn(proceed);
    }

    @Test
    public void shouldReturnResultOfContextProceed() throws Exception {
        assertThat(interceptor.aroundReadFrom(context), equalTo(proceed));
    }

    @Test
    public void shouldSetInputStreamToOriginalPayload() throws Exception {
        interceptor.aroundReadFrom(context);
        verify(context).setInputStream(argThat(inputStreamEqualTo(PAYLOAD)));
    }

    @Test
    public void shouldValidatePayloadAgainstSchema() throws Exception {
        interceptor.aroundReadFrom(context);
        verify(validator).validate(PAYLOAD, NAME);
    }

    @Test
    public void shouldSkipValidationIfNonJsonPayloadType() throws Exception {
        when(context.getMediaType()).thenReturn(new MediaType(MEDIA_TYPE, NON_JSON_MEDIA_SUBTYPE));
        interceptor.aroundReadFrom(context);
        verify(validator, never()).validate(PAYLOAD, NAME);
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
