package uk.gov.justice.services.test.utils.alfresco.stub;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;

import javax.json.JsonArray;

import org.junit.Test;

public class RequestRecorderTest {

    private RequestRecorder requestRecorder = new RequestRecorder();

    @Test
    public void shouldRecordRequest() throws Exception {

        requestRecorder.recordUploadRequest("fileABCDE.txt", new ByteArrayInputStream("someContentABCD".getBytes()), "userId1");
        requestRecorder.recordUploadRequest("file2.txt", new ByteArrayInputStream("someContentEFGH".getBytes()), "userId2");

        final JsonArray recordedRequests = requestRecorder.recordedRequests();
        assertThat(recordedRequests.size(), is(2));
        assertThat(recordedRequests.getJsonObject(0).getString("fileName"), is("fileABCDE.txt"));
        assertThat(recordedRequests.getJsonObject(0).getString("fileContent"), is("someContentABCD"));
        assertThat(recordedRequests.getJsonObject(0).getString("userId"), is("userId1"));

        assertThat(recordedRequests.getJsonObject(1).getString("fileName"), is("file2.txt"));
        assertThat(recordedRequests.getJsonObject(1).getString("fileContent"), is("someContentEFGH"));
        assertThat(recordedRequests.getJsonObject(1).getString("userId"), is("userId2"));

    }

    @Test
    public void shouldReset() throws Exception {
        requestRecorder.recordUploadRequest("fileABCDE.txt", new ByteArrayInputStream("someContentABCD".getBytes()), "");
        requestRecorder.reset();

        assertThat(requestRecorder.recordedRequests().size(), is(0));


    }
}