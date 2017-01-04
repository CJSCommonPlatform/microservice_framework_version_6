package uk.gov.justice.services.generators.commons.client;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.generators.commons.client.ActionMimeTypes.actionWithRequestAndResponseOf;
import static uk.gov.justice.services.generators.commons.client.ActionMimeTypes.actionWithRequestOf;
import static uk.gov.justice.services.generators.commons.client.ActionMimeTypes.actionWithResponseOf;

import org.junit.Test;
import org.raml.model.MimeType;

public class ActionMimeTypesTest {

    @Test
    public void shouldCreateWithRequestTypeAndReturnRequestTypeAsNameType() throws Exception {
        final MimeType mimeType = mock(MimeType.class);
        assertThat(actionWithRequestOf(mimeType).getNameType(), sameInstance(mimeType));
    }

    @Test
    public void shouldCreateWithRequestTypeAndReturnRequestTypeAsResponeType() throws Exception {
        final MimeType mimeType = mock(MimeType.class);
        assertThat(actionWithRequestOf(mimeType).getResponseType(), sameInstance(mimeType));
    }

    @Test
    public void shouldCreateWithResponseTypeAndReturnResponseTypeAsNameType() throws Exception {
        final MimeType mimeType = mock(MimeType.class);
        assertThat(actionWithResponseOf(mimeType).getNameType(), sameInstance(mimeType));
    }

    @Test
    public void shouldCreateWithResponseTypeAndReturnResponseTypeAsResponeType() throws Exception {
        final MimeType mimeType = mock(MimeType.class);
        assertThat(actionWithResponseOf(mimeType).getResponseType(), sameInstance(mimeType));
    }

    @Test
    public void shouldCreateWithRequestTypeAndResponseTypeAndReturnRequestTypeAsNameType() throws Exception {
        final MimeType requestType = mock(MimeType.class);
        final MimeType responseType = mock(MimeType.class);

        assertThat(actionWithRequestAndResponseOf(requestType, responseType).getNameType(), sameInstance(requestType));
    }

    @Test
    public void shouldCreateWithRequestTypeAndResponseTypeAndAndReturnResponseTypeAsResponeType() throws Exception {
        final MimeType requestType = mock(MimeType.class);
        final MimeType responseType = mock(MimeType.class);

        assertThat(actionWithRequestAndResponseOf(requestType, responseType).getResponseType(), sameInstance(responseType));
    }
}