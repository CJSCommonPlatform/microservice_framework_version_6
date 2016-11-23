package uk.gov.justice.services.file.alfresco.sender;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.apache.openejb.util.NetworkUtil.getNextAvailablePort;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import uk.gov.justice.services.file.alfresco.common.AlfrescoRestClient;
import uk.gov.justice.services.file.api.FileOperationException;
import uk.gov.justice.services.file.api.sender.FileData;
import uk.gov.justice.services.file.api.sender.FileSender;
import uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;


public class AlfrescoFileSenderIT {

    private static final int PORT = getNextAvailablePort();
    private static final String USER_ID = "user1234";
    private static final String WEB_CONTEXT = "/alfresco";
    private static final String UPLOAD_PATH = "/service/case/upload";
    private static final String BASE_PATH = "http://localhost:%d/%s";

    private static FileSender fileSender;

    @Rule
    public WireMockRule wireMock = new WireMockRule(PORT);

    @BeforeClass
    public static void beforeClass() {
        fileSender = alfrescoFileSenderWith(basePathWithPort(PORT));

    }

    @Test
    public void shouldSendFileToAlfresco() throws Exception {
        stubFor(post(urlEqualTo(WEB_CONTEXT + UPLOAD_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(alfrescoResponseOf("not_used", "not_used"))));

        fileSender.send("file.txt", toInputStream("abcd1243"));

        verify(postRequestedFor(urlEqualTo(WEB_CONTEXT + UPLOAD_PATH))
                .withHeader(CONTENT_TYPE, containing("multipart/form-data; boundary="))
                .withHeader("cppuid", equalTo(USER_ID))
                .withRequestBody(containing("Content-Disposition: form-data; name=\"filedata\"; filename=\"file.txt\"\r\nContent-Type: text/plain\r\n\r\nabcd1243\r\n")));
    }

    @Test
    public void shouldReturnUploadedFileData() {
        final String fileId = "file1234";
        final String fileMimeType = "text/plain";

        stubFor(post(urlEqualTo(WEB_CONTEXT + UPLOAD_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(alfrescoResponseOf(fileId, fileMimeType))));

        final FileData fileData = fileSender.send("abc.txt", toInputStream("aa"));

        assertThat(fileData.fileId(), is(fileId));

    }

    @Test(expected = FileOperationException.class)
    public void shouldThrowExceptionIfAlfrescoNotAvailable() {
        alfrescoFileSenderWith(basePathWithPort(getNextAvailablePort()))
                .send("abc.txt", toInputStream("aa"));

    }

    @Test(expected = FileOperationException.class)
    public void shouldThrowExceptionIfAlfrescoReturnedUnexpectedResponseCode() {

        stubFor(post(urlEqualTo(WEB_CONTEXT + UPLOAD_PATH))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody(alfrescoResponseOf("file1234", "text/plain"))));

        fileSender.send("abc.txt", toInputStream("aa"));

    }

    @Test(expected = FileOperationException.class)
    public void shouldThrowExceptionIfAlfrescoResponseContainsEmptyBody() {
        stubFor(post(urlEqualTo(WEB_CONTEXT + UPLOAD_PATH))
                .willReturn(aResponse()
                        .withStatus(200)));

        fileSender.send("abc.txt", toInputStream("aa"));

    }

    private static FileSender alfrescoFileSenderWith(final String basePath) {
        AlfrescoFileSender fileSender = new AlfrescoFileSender();
        fileSender.alfrescoUploadPath = UPLOAD_PATH;
        fileSender.alfrescoUploadUser = USER_ID;
        fileSender.restClient = new AlfrescoRestClient();
        setField(fileSender.restClient, "alfrescoBaseUri", basePath);
        setField(fileSender.restClient, "proxyType", "none");
        return fileSender;
    }

    private static String basePathWithPort(final int port) {
        return format(BASE_PATH, port, WEB_CONTEXT);
    }

    private String alfrescoResponseOf(final String fileId, final String fileMimeType) {
        return "{\n" +
                "  \"nodeRef\": \"workspace://SpacesStore/" + fileId + "\",\n" +
                "  \"fileName\": \"test.txt\",\n" +
                "  \"fileMimeType\": \"" + fileMimeType + "\",\n" +
                "  \"status\": {\n" +
                "    \"code\": \"200\",\n" +
                "    \"name\": \"OK\",\n" +
                "    \"description\": \"Success\"\n" +
                "  }\n" +
                "}";
    }

}