package uk.gov.justice.services.example.cakeshop.command.api;

import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.services.file.api.sender.FileSender;

import org.junit.Test;
import org.junit.runner.RunWith;
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
        verify(fileSender).send("file.txt", "someContent".getBytes());
    }
}