package uk.gov.justice.services.adapter.rest.interceptor;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.adapter.rest.multipart.FileInputDetails;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileInputDetailsHandlerTest {

    @Mock
    private SingleFileInputDetailsService singleFileInputDetailsService;

    @Captor
    private ArgumentCaptor<JsonObject> metadataCaptor;

    @InjectMocks
    private FileInputDetailsHandler fileInputDetailsHandler;

    @Test
    public void shouldCreateMetadataAndStoreFile() throws Exception {

        final String fileName = "fileName";
        final UUID fileId = randomUUID();

        final FileInputDetails fileInputDetails = mock(FileInputDetails.class);

        when(fileInputDetails.getFileName()).thenReturn(fileName);

        when(singleFileInputDetailsService.store(eq(fileInputDetails), metadataCaptor.capture())).thenReturn(fileId);

        assertThat(fileInputDetailsHandler.store(fileInputDetails), is(fileId));

        final JsonObject metadata = metadataCaptor.getValue();

        with(metadata.toString())
                .assertThat("$.fileName", is(fileName))
        ;
    }
}
