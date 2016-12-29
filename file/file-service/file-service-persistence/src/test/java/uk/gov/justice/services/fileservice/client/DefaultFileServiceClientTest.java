package uk.gov.justice.services.fileservice.client;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.file.api.domain.StorableFile;
import uk.gov.justice.services.fileservice.repository.TransactionalFileRepository;

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
    private TransactionalFileRepository transactionalFileRepository;

    @InjectMocks
    private DefaultFileServiceClient defaultFileServiceClient;

    @Test
    public void shouldCallTheFileRepositoryWhenStoringAFile() throws Exception {

        final UUID fileId = UUID.randomUUID();
        final JsonObject metadata = mock(JsonObject.class);
        final byte[] content = "the file content".getBytes();

        final StorableFile storableFile = new StorableFile(
                fileId,
                metadata,
                content);

        defaultFileServiceClient.store(storableFile);

        verify(transactionalFileRepository).store(fileId, content, metadata);
    }

    @Test
    public void shouldCallTheFileRepositoryWhenFindingAFile() throws Exception {

        final UUID fileId = UUID.randomUUID();

        final Optional<StorableFile> storableFile = of(mock(StorableFile.class));

        when(transactionalFileRepository.find(fileId)).thenReturn(storableFile);

        assertThat(defaultFileServiceClient.find(fileId), is(storableFile));
    }
}
