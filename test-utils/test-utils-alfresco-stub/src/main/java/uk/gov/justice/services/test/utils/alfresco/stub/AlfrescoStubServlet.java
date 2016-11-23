package uk.gov.justice.services.test.utils.alfresco.stub;

import java.io.IOException;
import java.io.*;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;


@WebServlet(name = "uploadServlet", urlPatterns = {AlfrescoStubServlet.UPLOAD, AlfrescoStubServlet.RECORDED_REQUESTS, AlfrescoStubServlet.RESET})
@MultipartConfig
public class AlfrescoStubServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static final String UPLOAD = "/service/case/upload";
    static final String RECORDED_REQUESTS = "/recorded-requests";
    static final String RESET = "/reset";
    private static final String USER_ID_HEADER = "cppuid";
    private static final String FILEDATA_PART = "filedata";

    RequestRecorder requestRecorder = new RequestRecorder();

    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        final String requestURI = request.getRequestURI();
        if (requestURI.contains(UPLOAD)) {
            recordUpload(request);
            out.println(alfrescoResponseOf("123", "text/plain"));
        } else if(requestURI.contains(RECORDED_REQUESTS)) {
            out.println(requestRecorder.recordedRequests());
        } else if(requestURI.contains(RESET)) {
            requestRecorder.reset();
        }
        out.close();
    }

    private void recordUpload(final HttpServletRequest request) throws IOException, ServletException {
        final Part filePart = request.getPart(FILEDATA_PART);
        requestRecorder.recordUploadRequest(filePart.getSubmittedFileName(), filePart.getInputStream(), request.getHeader(USER_ID_HEADER));
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
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