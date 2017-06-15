package uk.gov.justice.services.test.utils.alfresco.stub;

import static javax.json.Json.createArrayBuilder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.json.JsonArray;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlfrescoStubServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter responseWriter;

    @Mock
    private RequestRecorder requestRecorder;

    @InjectMocks
    private AlfrescoStubServlet alfrescoStubServlet;

    @Before
    public void setUp() throws Exception {
        when(response.getWriter()).thenReturn(responseWriter);

    }

    @Test
    public void shouldRegisterUploadRequest() throws Exception {
        final String fileName = "file123.txt";
        final InputStream inputStream = new ByteArrayInputStream("" .getBytes());
        final String userId = "someUser";

        when(request.getRequestURI()).thenReturn("/alfresco-stub/service/case/upload");
        when(request.getHeader("cppuid")).thenReturn(userId);
        final Part part = mock(Part.class);

        when(part.getSubmittedFileName()).thenReturn(fileName);
        when(part.getInputStream()).thenReturn(inputStream);

        when(request.getPart("filedata")).thenReturn(part);


        alfrescoStubServlet.doPost(request, response);

        verify(requestRecorder).recordUploadRequest(fileName, inputStream, userId);

    }

    @Test
    public void shouldReturnRecordedRequestData() throws Exception {
        when(request.getRequestURI()).thenReturn("/alfresco-stub/recorded-requests");

        final JsonArray jsonObject = createArrayBuilder().add("dummmy").build();
        when(requestRecorder.recordedRequests()).thenReturn(jsonObject);

        alfrescoStubServlet.doGet(request,response);

        verify(responseWriter).println(jsonObject);

    }

    @Test
    public void shouldResetRecorder() throws Exception {
        when(request.getRequestURI()).thenReturn("/alfresco-stub/reset");

        alfrescoStubServlet.doGet(request,response);

        verify(requestRecorder).reset();
    }
}
