package uk.gov.justice.services.example.cakeshop.command.api;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

import uk.gov.justice.services.file.api.sender.FileSender;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddRecipeFileCommandApiTest {

    @Mock
    private FileSender fileSender;

    @InjectMocks
    private AddRecipeFileCommandApi handler;

    @Test
    public void shouldPassContentToFileSender() throws Exception {
        handler.addRecipeFile(envelope().withPayloadOf("file.txt", "fileName").withPayloadOf("someContent", "fileContent").build());

        ArgumentCaptor<InputStream> content = ArgumentCaptor.forClass(InputStream.class);

        verify(fileSender).send(eq("file.txt"), content.capture());
        assertThat(IOUtils.toString(content.getValue()), is("someContent"));
    }
}