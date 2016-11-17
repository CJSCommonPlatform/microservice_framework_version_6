package uk.gov.justice.services.file.alfresco.requester;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.apache.openejb.util.NetworkUtil.getNextAvailablePort;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;

import uk.gov.justice.services.file.alfresco.rest.AlfrescoRestClient;
import uk.gov.justice.services.file.api.FileData;
import uk.gov.justice.services.file.api.FileServiceUnavailableException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;


public class AlfrescoFileRequesterIT {

    private static int PORT = getNextAvailablePort();
    private static final String BASE_PATH = format("http://localhost:%d/alfresco", PORT);
    public static final String ALFRESCO_WORKSPACE_PATH = "/service/api/node/content/workspace/SpacesStore/";

    private static AlfrescoFileRequester fileRequester;

    @Rule
    public WireMockRule wireMock = new WireMockRule(PORT);

    @BeforeClass
    public static void beforeClass() {
        fileRequester = new AlfrescoFileRequester();
        fileRequester.alfrescoWorkspacePath = ALFRESCO_WORKSPACE_PATH;
        fileRequester.alfrescoReadUser = "user1234";
        fileRequester.restClient = new AlfrescoRestClient();
        fileRequester.restClient.alfrescoBaseUri = BASE_PATH;
        fileRequester.restClient.proxyType = "none";
    }

    @Test
    public void shouldRequestFileFromAlfresco() throws Exception {
        final String fileId = randomUUID().toString();
        final String mimeType = "text/plain";
        final String fileName = "file.txt";
        final FileData fileData = new FileData(fileId, mimeType);

        fileRequester.request(fileData , fileName);

        verify(getRequestedFor(urlEqualTo("/alfresco" + ALFRESCO_WORKSPACE_PATH + fileId + "/content/"+ fileName))
               .withHeader(ID, equalTo("user1234")));
    }

    @Test
    public void shouldReturnResponseFromAlfresco(){
        final String fileId = randomUUID().toString();
        final String mimeType = "text/plain";
        final String fileName = "file.txt";
        final FileData fileData = new FileData(fileId, mimeType);
        final String fileContent = "abcd";
        stubFor(get(urlEqualTo("/alfresco" + ALFRESCO_WORKSPACE_PATH + fileId + "/content/"+ fileName))
                .withHeader(ID, equalTo("user1234")).willReturn(aResponse().withBody(fileContent)));
        assertArrayEquals(fileRequester.request(fileData , fileName).get(), fileContent.getBytes());
    }

    @Test
    public void shouldReturnOptionalEmptyFromAlfrescoIfFileIsNotFound(){
        final String fileId = randomUUID().toString();
        final String mimeType = "text/plain";
        final String fileName = "file.txt";
        final FileData fileData = new FileData(fileId, mimeType);
        stubFor(get(urlEqualTo("/alfresco" + ALFRESCO_WORKSPACE_PATH + fileId + "/content/"+ fileName))
                .withHeader(ID, equalTo("user1234")).willReturn(aResponse().withStatus(404)));
        assertFalse(fileRequester.request(fileData , fileName).isPresent());
    }

    @Test(expected=FileServiceUnavailableException.class)
    public void shouldThrowAnExceptionIfAlfrescoServiceReturnedError(){
        final String fileId = randomUUID().toString();
        final String mimeType = "text/plain";
        final String fileName = "file.txt";
        final FileData fileData = new FileData(fileId, mimeType);
        stubFor(get(urlEqualTo("/alfresco" + ALFRESCO_WORKSPACE_PATH + fileId + "/content/"+ fileName))
                .withHeader(ID, equalTo("user1234")).willReturn(aResponse().withStatus(500)));
        fileRequester.request(fileData , fileName);
    }

    @Test(expected=FileServiceUnavailableException.class)
    public void shouldThrowAnExceptionIfAlfrescoServiceIsUnavailable(){
        final AlfrescoFileRequester alfrescoFileRequester = getUnMockedAlfrescoFileRequester();
        final String fileId = randomUUID().toString();
        final String mimeType = "text/plain";
        final String fileName = "file.txt";
        final FileData fileData = new FileData(fileId, mimeType);
        alfrescoFileRequester.request(fileData , fileName);
    }

    private AlfrescoFileRequester getUnMockedAlfrescoFileRequester() {
        final int port = getNextAvailablePort();
        final String basePath = format("http://localhost:%d/alfresco", port);
        final String alfrescoWorkspacePath = "/service/api/node/content/workspace/SpacesStore/";
        final AlfrescoFileRequester alfrescoFileRequester = new AlfrescoFileRequester();
        alfrescoFileRequester.alfrescoWorkspacePath = alfrescoWorkspacePath;
        alfrescoFileRequester.alfrescoReadUser = "user1234";
        alfrescoFileRequester.restClient = new AlfrescoRestClient();
        alfrescoFileRequester.restClient.alfrescoBaseUri = basePath;
        alfrescoFileRequester.restClient.proxyType = "none";
        return alfrescoFileRequester;
    }

}