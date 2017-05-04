package uk.gov.justice.services.file.alfresco.requester;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

import org.hamcrest.CoreMatchers;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.openejb.util.NetworkUtil.getNextAvailablePort;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import uk.gov.justice.services.file.alfresco.common.AlfrescoRestClient;
import uk.gov.justice.services.file.api.FileOperationException;
import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MultivaluedMap;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;


public class AlfrescoFileRequesterIT {

    private static final String BASE_PATH = "http://localhost:%d/alfresco";
    private static final String UNUSED_MIME_TYPE = "text/plain";
    private static final String UNUSED_FILE_NAME = "file.txt";
    private static final String ALFRESCO_WORKSPACE_PATH = "/service/api/node/content/workspace/SpacesStore/";
    private static final String ALFRESCO_WORKSPACE_TRANSFORMATION_PATH = "service/api/node/workspace/SpacesStore/";


    private static int PORT = getNextAvailablePort();

    private static AlfrescoFileRequester fileRequester;

    private RestClient restClient;

    private MultivaluedMap<String,Object> headers;

    @Rule
    public WireMockRule wireMock = new WireMockRule(PORT);

    @BeforeClass
    public static void beforeClass() {
        final int port = PORT;
        fileRequester = alfrescoFileRequesterWith(basePathWithPort(port));
    }

    @Test
    public void shouldRequestFileFromAlfrescoInAttachmentMode() throws Exception {
        final String fileId = randomUUID().toString();
        final String fileName = "file.txt";
        fileRequester.request(fileId, UNUSED_MIME_TYPE, fileName);

        verify(getRequestedFor(urlEqualTo(format("/alfresco%s%s/content/%s", ALFRESCO_WORKSPACE_PATH, fileId, fileName)))
                .withHeader("cppuid", equalTo("user1234")));
    }

    @Test
    public void shouldReturnResponseFromAlfresco() throws IOException {
        final String fileId = randomUUID().toString();
        final String mimeType = "text/plain";
        final String fileName = "file123.txt";
        final String fileContent = "abcd";

        stubFor(get(urlMatching(format("/alfresco%s%s/content/%s", ALFRESCO_WORKSPACE_PATH, fileId, fileName)))
                .withHeader("cppuid", equalTo("user1234"))
                .willReturn(aResponse().withHeader("Content-Type", TEXT_PLAIN).withBody(fileContent)));

        final Optional<InputStream> inputStream = fileRequester.request(fileId, mimeType, fileName);
        assertTrue(inputStream.isPresent());

        assertThat(IOUtils.toString(inputStream.get()), is(fileContent));


    }

    @Test
    public void shouldReturnPdfContentFromAlfresco() throws IOException {
        final String fileId = randomUUID().toString();
        final String mimeType = "application/pdf";
        final String fileName = "file123.txt";

        restClient = new RestClient();

        headers = new MultivaluedMapImpl<>();
        headers.add("cppuid", "user1234");

        final Response readMaterialResponseAsPdf = restClient.query(format(basePathWithPort(PORT) + "/%s%s?transformpdf=true", ALFRESCO_WORKSPACE_TRANSFORMATION_PATH, fileId), "*/*", headers);

        assertThat(readMaterialResponseAsPdf.getStatus(), is(OK.getStatusCode()));
        assertThat(readMaterialResponseAsPdf.getHeaderString(CONTENT_TYPE), CoreMatchers.equalTo("application/pdf;charset=UTF-8"));

        final Optional<InputStream> inputStream = fileRequester.request(fileId, mimeType, fileName, true);
        assertTrue(inputStream.isPresent());

        assertEquals(IOUtils.toString(inputStream.get()).length(), readMaterialResponseAsPdf.readEntity(String.class).length());
    }

    @Test
    public void shouldReturnOriginalContentFromAlfresco() throws IOException {
        final String fileId = randomUUID().toString();
        final String mimeType = "text/plain";
        final String fileName = "file123.txt";

        restClient = new RestClient();

        headers = new MultivaluedMapImpl<>();
        headers.add("cppuid", "user1234");

        final Response readMaterialResponseAsPdf = restClient.query(format(basePathWithPort(PORT) + "/%s%s?transformpdf=false", ALFRESCO_WORKSPACE_TRANSFORMATION_PATH, fileId), "*/*", headers);

        assertThat(readMaterialResponseAsPdf.getStatus(), is(OK.getStatusCode()));
        assertThat(readMaterialResponseAsPdf.getHeaderString(CONTENT_TYPE), CoreMatchers.equalTo("text/plain;charset=UTF-8"));

        final Optional<InputStream> inputStream = fileRequester.request(fileId, mimeType, fileName, false);
        assertTrue(inputStream.isPresent());

        assertEquals(IOUtils.toString(inputStream.get()).length(), readMaterialResponseAsPdf.readEntity(String.class).length());
    }

    @Test
    public void shouldReturnOptionalEmptyFromAlfrescoIfFileIsNotFound() {
        final String fileId = randomUUID().toString();
        final String mimeType = "text/xml";
        final String fileName = "file5.xml";

        stubFor(get(urlEqualTo(format("/alfresco%s%s/content/%s", ALFRESCO_WORKSPACE_PATH, fileId, fileName)))
                .withHeader("cppuid", equalTo("user1234")).willReturn(aResponse().withStatus(404)));

        assertFalse(fileRequester.request(fileId, mimeType, fileName).isPresent());
    }

    @Test
    public void shouldThrowAnExceptionIfAlfrescoServiceReturnedError() {
        final String fileId = randomUUID().toString();
        final String fileName = "file.txt";

        stubFor(get(urlEqualTo(format("/alfresco%s%s/content/%s", ALFRESCO_WORKSPACE_PATH, fileId, fileName)))
                .withHeader("cppuid", equalTo("user1234")).willReturn(aResponse().withStatus(500)));

        try {
            fileRequester.request(fileId, UNUSED_MIME_TYPE, fileName);
            fail("Was expecting a FileOperationException to be thrown");
        } catch (final FileOperationException foe) {
            assertEquals(format("Error fetching %s from Alfresco with fileId = %s", fileName, fileId), foe.getMessage());
        }
    }

    @Test
    public void shouldThrowAnExceptionIfAlfrescoServiceIsUnavailable() {
        final String fileId = randomUUID().toString();
        try {
            alfrescoFileRequesterWith(basePathWithPort(getNextAvailablePort()))
                    .request(fileId, UNUSED_MIME_TYPE, UNUSED_FILE_NAME);
            fail("Was expecting a FileOperationException to be thrown");
        } catch (final FileOperationException foe) {
            assertTrue(foe.getCause() instanceof ProcessingException);
        }
    }

    private static AlfrescoFileRequester alfrescoFileRequesterWith(final String basePath) {
        AlfrescoFileRequester fileRequester = new AlfrescoFileRequester();
        fileRequester.alfrescoWorkspacePath = ALFRESCO_WORKSPACE_PATH;
        fileRequester.alfrescoPdfContentWorkspacePath = "/" + ALFRESCO_WORKSPACE_TRANSFORMATION_PATH;
        fileRequester.alfrescoReadUser = "user1234";
        fileRequester.restClient = new AlfrescoRestClient();
        setField(fileRequester.restClient, "alfrescoBaseUri", basePath);
        setField(fileRequester.restClient, "proxyType", "none");
        return fileRequester;
    }

    private static String basePathWithPort(final int port) {
        return format(BASE_PATH, port);
    }

}