package uk.gov.justice.services.adapter.rest.envelope;

import static java.nio.charset.Charset.defaultCharset;
import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.MediaType;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class MediaTypesTest {

    private static final String MEDIA_TYPE_TYPE = "application";
    private static final String MEDIA_SUBTYPE = "vnd.test-name+json";
    private static final MediaType MEDIA_TYPE = new MediaType(MEDIA_TYPE_TYPE, MEDIA_SUBTYPE);

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(MediaTypes.class);
    }

    @Test
    public void shouldReturnDefaultCharsetIfCharsetNotSetInMediaType() {
        final String charsetResult = MediaTypes.charsetFrom(MEDIA_TYPE);

        assertThat(charsetResult, CoreMatchers.is(defaultCharset().name()));
    }

    @Test
    public void shouldReturnCharsetIfCharsetSetInMediaType() {
        final String charset = "UTF_16";
        final String charsetResult = MediaTypes.charsetFrom(MEDIA_TYPE.withCharset(charset));

        assertThat(charsetResult, CoreMatchers.is(charset));
    }
}