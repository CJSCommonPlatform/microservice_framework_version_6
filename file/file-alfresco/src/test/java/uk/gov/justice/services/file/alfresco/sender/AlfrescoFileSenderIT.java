package uk.gov.justice.services.file.alfresco.sender;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static org.apache.openejb.util.NetworkUtil.getNextAvailablePort;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.file.alfresco.rest.AlfrescoRestClient;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;


public class AlfrescoFileSenderIT {

    private static int PORT = getNextAvailablePort();
    private static final String BASE_PATH = format("http://localhost:%d/alfresco", PORT);
    public static final String UPLOAD_PATH = "/service/case/upload";

    private static AlfrescoFileSender fileSender;

    @Rule
    public WireMockRule wireMock = new WireMockRule(PORT);

    @BeforeClass
    public static void beforeClass() {
        fileSender = new AlfrescoFileSender();
        fileSender.alfrescoUploadPath = UPLOAD_PATH;
        fileSender.alfrescoUploadUser = "user1234";
        fileSender.objectMapper = new ObjectMapperProducer().objectMapper();
        fileSender.restClient = new AlfrescoRestClient();
        fileSender.restClient.alfrescoBaseUri = BASE_PATH;
        fileSender.restClient.proxyType = "none";

    }

    @Test
    public void shouldSendFileToAlfresco() throws Exception {

        fileSender.send("file.txt", "abcd1243".getBytes());

        verify(postRequestedFor(urlEqualTo("/alfresco" + UPLOAD_PATH))
                .withHeader(CONTENT_TYPE, equalTo(MULTIPART_FORM_DATA_TYPE.toString()))
                .withRequestBody(equalTo("abcd1243")));


    }

}