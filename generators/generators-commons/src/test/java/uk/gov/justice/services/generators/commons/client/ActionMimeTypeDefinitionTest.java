package uk.gov.justice.services.generators.commons.client;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.generators.commons.client.ActionMimeTypeDefinition.definitionWithRequest;
import static uk.gov.justice.services.generators.commons.client.ActionMimeTypeDefinition.definitionWithRequestAndResponse;
import static uk.gov.justice.services.generators.commons.client.ActionMimeTypeDefinition.definitionWithResponse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raml.model.MimeType;

public class ActionMimeTypeDefinitionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldCreateWithRequestTypeAndReturnRequestTypeAsNameType() throws Exception {
        final MimeType mimeType = mock(MimeType.class);
        assertThat(definitionWithRequest(mimeType).getNameType(), sameInstance(mimeType));
    }

    @Test
    public void shouldCreateWithRequestTypeAndReturnRequestTypeAsResponeType() throws Exception {
        final MimeType mimeType = mock(MimeType.class);
        assertThat(definitionWithRequest(mimeType).getResponseType(), sameInstance(mimeType));
    }

    @Test
    public void shouldThrowExceptionForDefinitionWithRequestIfNullRequest() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("A RAML action must have either a request or response mimetype");

        definitionWithRequest(null).getResponseType();
    }

    @Test
    public void shouldCreateWithResponseTypeAndReturnResponseTypeAsNameType() throws Exception {
        final MimeType mimeType = mock(MimeType.class);
        assertThat(definitionWithResponse(mimeType).getNameType(), sameInstance(mimeType));
    }

    @Test
    public void shouldCreateWithResponseTypeAndReturnResponseTypeAsResponeType() throws Exception {
        final MimeType mimeType = mock(MimeType.class);
        assertThat(definitionWithResponse(mimeType).getResponseType(), sameInstance(mimeType));
    }

    @Test
    public void shouldThrowExceptionForDefinitionWithResponseIfNullRequest() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("A RAML action must have either a request or response mimetype");

        definitionWithResponse(null).getResponseType();
    }

    @Test
    public void shouldCreateWithRequestTypeAndResponseTypeAndReturnRequestTypeAsNameType() throws Exception {
        final MimeType requestType = mock(MimeType.class);
        final MimeType responseType = mock(MimeType.class);

        assertThat(definitionWithRequestAndResponse(requestType, responseType).getNameType(), sameInstance(requestType));
    }

    @Test
    public void shouldCreateWithRequestTypeAndResponseTypeAndAndReturnResponseTypeAsResponseType() throws Exception {
        final MimeType requestType = mock(MimeType.class);
        final MimeType responseType = mock(MimeType.class);

        assertThat(definitionWithRequestAndResponse(requestType, responseType).getResponseType(), sameInstance(responseType));
    }

    @Test
    public void shouldThrowExceptionForNullRequest() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("A RAML action must have either a request or response mimetype");

        definitionWithRequestAndResponse(null, mock(MimeType.class)).getResponseType();
    }

    @Test
    public void shouldThrowExceptionForNullResponse() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("A RAML action must have either a request or response mimetype");

        definitionWithRequestAndResponse(mock(MimeType.class), null).getResponseType();
    }

    @Test
    public void shouldThrowExceptionForNullRequestAndNullResponse() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("A RAML action must have either a request or response mimetype");

        definitionWithRequestAndResponse(null, null).getResponseType();
    }
}