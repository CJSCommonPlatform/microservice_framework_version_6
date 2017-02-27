package uk.gov.justice.services.adapters.rest.helper;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.MimeType;

@RunWith(MockitoJUnitRunner.class)
public class MultipartsTest {

    @Mock
    private MimeType mimeType;

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(Multiparts.class);
    }

    @Test
    public void shouldReturnTrueForApplicationFormUrlencodedType() throws Exception {
        when(mimeType.getType()).thenReturn(APPLICATION_FORM_URLENCODED);

        assertThat(Multiparts.isMultipartResource(mimeType), is(true));
    }

    @Test
    public void shouldReturnTrueForMultipartFormDataType() throws Exception {
        when(mimeType.getType()).thenReturn(MULTIPART_FORM_DATA);

        assertThat(Multiparts.isMultipartResource(mimeType), is(true));
    }

    @Test
    public void shouldReturnFalseForAnyOtherType() throws Exception {
        when(mimeType.getType()).thenReturn("application/vnd.test+json");

        assertThat(Multiparts.isMultipartResource(mimeType), is(false));
    }
}