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
import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.apache.openejb.util.NetworkUtil.getNextAvailablePort;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import uk.gov.justice.services.file.alfresco.common.AlfrescoRestClient;
import uk.gov.justice.services.file.api.FileOperationException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

import javax.ws.rs.ProcessingException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;


public class AlfrescoFileRequesterIT {

    private static final String BASE_PATH = "http://localhost:%d/alfresco";
    private static final String UNUSED_MIME_TYPE = "text/plain";
    private static final String UNUSED_FILE_NAME = "file.txt";
    private static final String ALFRESCO_WORKSPACE_PATH = "/service/api/node/content/workspace/SpacesStore/";

    private static int PORT = getNextAvailablePort();

    private static AlfrescoFileRequester fileRequester;

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

        verify(getRequestedFor(urlEqualTo(format("/alfresco%s%s/content/%s?a=true", ALFRESCO_WORKSPACE_PATH, fileId, fileName)))
                .withHeader("cppuid", equalTo("user1234")));
    }

    @Test
    public void shouldRequestFileFromAlfrescoWhenRequestingAsStreamed() throws Exception {
        final String fileId = randomUUID().toString();
        final String fileName = "file.txt";

        fileRequester.requestStreamed(fileId, UNUSED_MIME_TYPE, fileName);

        verify(getRequestedFor(urlEqualTo(format("/alfresco%s%s/content/%s", ALFRESCO_WORKSPACE_PATH, fileId, fileName)))
                .withHeader("cppuid", equalTo("user1234")));
    }

    @Test
    public void shouldReturnResponseFromAlfresco() {
        final String fileId = randomUUID().toString();
        final String mimeType = "text/plain";
        final String fileName = "file123.txt";
        final String fileContent = "abcd";

        stubFor(get(urlEqualTo(format("/alfresco%s%s/content/%s?a=true", ALFRESCO_WORKSPACE_PATH, fileId, fileName)))
                .withHeader("cppuid", equalTo("user1234")).willReturn(aResponse().withBody(fileContent)));

        final Optional<byte[]> responseData = fileRequester.request(fileId, mimeType, fileName);

        assertTrue(responseData.isPresent());
        assertArrayEquals(fileContent.getBytes(), responseData.get());
    }

    @Test
    public void shouldReturnResponseFromAlfrescoAsStreamed() {
        final String fileId = randomUUID().toString();
        final String mimeType = "text/plain";
        final String fileName = "file123.txt";
        final String fileContent = "abcd";

        stubFor(get(urlMatching(format("/alfresco%s%s/content/%s", ALFRESCO_WORKSPACE_PATH, fileId, fileName)))
                .withHeader("cppuid", equalTo("user1234"))
                .willReturn(aResponse().withHeader("Content-Type", TEXT_PLAIN).withBody(fileContent)));

        final Optional<InputStream> inputStream = fileRequester.requestStreamed(fileId, mimeType, fileName);
        assertTrue(inputStream.isPresent());

        final String result = new BufferedReader(new InputStreamReader(inputStream.get())).lines()
                .parallel().collect(joining("\n"));
        assertEquals(fileContent, result);
    }

    @Test
    public void shouldReturnOptionalEmptyFromAlfrescoIfFileIsNotFound() {
        final String fileId = randomUUID().toString();
        final String mimeType = "text/xml";
        final String fileName = "file5.xml";

        stubFor(get(urlEqualTo(format("/alfresco%s%s/content/%s?a=true", ALFRESCO_WORKSPACE_PATH, fileId, fileName)))
                .withHeader("cppuid", equalTo("user1234")).willReturn(aResponse().withStatus(404)));

        assertFalse(fileRequester.request(fileId, mimeType, fileName).isPresent());
    }

    @Test
    public void shouldReturnOptionalEmptyFromAlfrescoIfFileIsNotFoundWhenRequestingAsStreamed() {
        final String fileId = randomUUID().toString();
        final String mimeType = "text/xml";
        final String fileName = "file5.xml";

        stubFor(get(urlEqualTo(format("/alfresco%s%s/content/%s", ALFRESCO_WORKSPACE_PATH, fileId, fileName)))
                .withHeader("cppuid", equalTo("user1234")).willReturn(aResponse().withStatus(404)));

        assertFalse(fileRequester.requestStreamed(fileId, mimeType, fileName).isPresent());
    }

    @Test
    public void shouldThrowAnExceptionIfAlfrescoServiceReturnedError() {
        final String fileId = randomUUID().toString();
        final String fileName = "file.txt";

        stubFor(get(urlEqualTo(format("/alfresco%s%s/content/%s?a=true", ALFRESCO_WORKSPACE_PATH, fileId, fileName)))
                .withHeader("cppuid", equalTo("user1234")).willReturn(aResponse().withStatus(500)));

        try {
            fileRequester.request(fileId, UNUSED_MIME_TYPE, fileName);
            fail("Was expecting a FileOperationException to be thrown");
        } catch (final FileOperationException foe) {
            assertEquals("Alfresco is unavailable with response status code: 500", foe.getMessage());
        }
    }

    @Test
    public void shouldThrowAnExceptionIfAlfrescoServiceReturnedErrorWhenRequestingAsStreamed() {
        final String fileId = randomUUID().toString();
        final String fileName = "file.txt";

        stubFor(get(urlEqualTo(format("/alfresco%s%s/content/%s", ALFRESCO_WORKSPACE_PATH, fileId, fileName)))
                .withHeader("cppuid", equalTo("user1234")).willReturn(aResponse().withStatus(500)));

        try {
            fileRequester.requestStreamed(fileId, UNUSED_MIME_TYPE, fileName);
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

    @Test
    public void shouldThrowAnExceptionIfAlfrescoServiceIsUnavailableWhenRequestingAsStreamed() {
        final String fileId = randomUUID().toString();
        try {
            alfrescoFileRequesterWith(basePathWithPort(getNextAvailablePort()))
                    .requestStreamed(fileId, UNUSED_MIME_TYPE, UNUSED_FILE_NAME);
            fail("Was expecting a FileOperationException to be thrown");
        } catch (final FileOperationException foe) {
            assertTrue(foe.getCause() instanceof ProcessingException);
        }
    }

    private static AlfrescoFileRequester alfrescoFileRequesterWith(final String basePath) {
        AlfrescoFileRequester fileRequester = new AlfrescoFileRequester();
        fileRequester.alfrescoWorkspacePath = ALFRESCO_WORKSPACE_PATH;
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