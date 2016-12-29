package uk.gov.justice.services.fileservice.repository;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.file.api.domain.StorableFile;

import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class DefaultFileServiceClientTest {

    @Mock
    private MetadataJdbcRepository metadataJdbcRepository;

    @Mock
    private FileJdbcRepository fileJdbcRepository;

    @InjectMocks
    private DefaultFileServiceClient defaultFileServiceClient;

    @Test
    public void shouldStoreAFileInTheFileAndMetadataTables() throws Exception {

        final UUID fileId = randomUUID();
        final JsonObject metadataJsonObject = mock(JsonObject.class);
        final byte[] content = "the file content".getBytes();

        final StorableFile storableFile = new StorableFile(fileId, metadataJsonObject, content);

        when(fileJdbcRepository.findByFileId(fileId)).thenReturn(of(content));

        defaultFileServiceClient.store(storableFile);

        verify(fileJdbcRepository).update(fileId, content);
        verify(metadataJdbcRepository).update(fileId, metadataJsonObject);
    }

    @Test
    public void shouldFindAFileUsingTheFileAndMetadataTables() throws Exception {

        final UUID fileId = randomUUID();
        final byte[] content = "the file content".getBytes();

        final File file = mock(File.class);
        final Metadata metadata = mock(Metadata.class);
        final JsonObject metadataJsonObject = mock(JsonObject.class);

        when(fileJdbcRepository.findByFileId(fileId)).thenReturn(of(content));
        when(metadataJdbcRepository.findByFileId(fileId)).thenReturn(of(metadataJsonObject));
        when(metadata.getJson()).thenReturn(metadataJsonObject);
        when(file.getContent()).thenReturn(content);

        final Optional<StorableFile> storableFile = defaultFileServiceClient.find(fileId);

        assertThat(storableFile.isPresent(), is(true));

        assertThat(storableFile.get().getFileId(), is(fileId));
        assertThat(storableFile.get().getMetadata(), is(metadataJsonObject));
        assertThat(storableFile.get().getContent(), is(content));
    }

    @Test
    public void shouldReturnEmptyIfNoFileFoundInTheDatabase() throws Exception {

        final UUID fileId = randomUUID();

        when(fileJdbcRepository.findByFileId(fileId)).thenReturn(empty());

        final Optional<StorableFile> storableFile = defaultFileServiceClient.find(fileId);

        assertThat(storableFile.isPresent(), is(false));
    }

    @Test
    public void shouldThrowAnExceptionIfTheFileIsFoundButWithNoMetadataFound() throws Exception {

        final UUID fileId = randomUUID();
        final byte[] content = "the file content".getBytes();

        when(fileJdbcRepository.findByFileId(fileId)).thenReturn(of(content));
        when(metadataJdbcRepository.findByFileId(fileId)).thenReturn(empty());

        try {
            defaultFileServiceClient.find(fileId);
            fail();
        } catch (final RuntimeException expected) {
            assertThat(expected.getMessage(), is("Found file with id '" + fileId + "', but with no metadata in database"));
        }
    }
}
