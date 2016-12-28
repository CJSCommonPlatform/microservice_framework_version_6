package uk.gov.justice.services.fileservice.repository;


import static java.util.UUID.randomUUID;
import static javax.json.Json.createReader;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.StringReader;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.postgresql.Driver;

public class FileJdbcRepositoryIT {


    private static final String URL = "jdbc:postgresql://localhost/fileservice";
    private static final String USERNAME = "fileservice";
    private static final String PASSWORD = "fileservice";

    private final FileJdbcRepository fileJdbcRepository = new FileJdbcRepository();
    private final MetadataRepository metadataRepository = new MetadataRepository();

    @Before
    public void setupDataSource() {

        final TestDataSourceProvider dataSourceProvider = new TestDataSourceProvider(URL, USERNAME, PASSWORD);
        fileJdbcRepository.dataSourceProvider = dataSourceProvider;
        metadataRepository.dataSourceProvider = dataSourceProvider;
        metadataRepository.jsonSetter = new PostgresJsonSetter();
    }

    @Test
    public void shouldStoreAndRetrieveFileData() {

        final UUID fileId = randomUUID();
        final File file = new File(fileId, "file-name".getBytes());
        fileJdbcRepository.insert(file);

        final Optional<File> fileFound = fileJdbcRepository.findByFileId(fileId);

        assertThat(fileFound.isPresent(), is(true));

        assertThat(fileFound, notNullValue());
        assertThat(fileFound.get().getFileId(), is(file.getFileId()));
        assertThat(fileFound.get().getContent(), is(file.getContent()));
    }

    @Test
    public void shouldStoreAndRetrieveMetadata() {

        final UUID fileId = randomUUID();
        final UUID metadataId = randomUUID();

        final String json = "{\"some\": \"json\"}";
        final File file = new File(fileId, "some file or other".getBytes());
        final Metadata metadata = new Metadata(metadataId, toJsonObject(json), fileId);

        fileJdbcRepository.insert(file);
        metadataRepository.insert(metadata);

        final Optional<Metadata> foundMetadata = metadataRepository.findByFileId(fileId);

        assertThat(foundMetadata.isPresent(), is(true));
        assertThat(foundMetadata.get(), is(metadata));
    }

    private JsonObject toJsonObject(final String json) {
        return createReader(new StringReader(json)).readObject();
    }
}
