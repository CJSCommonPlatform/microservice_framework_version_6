package uk.gov.justice.services.fileservice.repository;


import static java.util.UUID.randomUUID;
import static javax.json.Json.createReader;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.fileservice.datasource.TestDataSourceProvider;

import java.io.StringReader;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.Before;
import org.junit.Test;

public class FileAndMetadataPersistenceTest {

    private static final String LIQUIBASE_FILE_STORE_DB_CHANGELOG_XML = "liquibase/file-service-liquibase-db-changelog.xml";

    private static final String URL = "jdbc:h2:mem:test;MV_STORE=FALSE;MVCC=FALSE";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "sa";
    private static final String DRIVER_CLASS = org.h2.Driver.class.getName();

    private final FileJdbcRepository fileJdbcRepository = new FileJdbcRepository();
    private final MetadataJdbcRepository metadataJdbcRepository = new MetadataJdbcRepository();

    private static final TestDataSourceProvider DATA_SOURCE_PROVIDER = new TestDataSourceProvider(
            URL,
            USERNAME,
            PASSWORD,
            DRIVER_CLASS);

    @Before
    public void setupDataSource() throws Exception {

        fileJdbcRepository.dataSourceProvider = DATA_SOURCE_PROVIDER;
        metadataJdbcRepository.dataSourceProvider = DATA_SOURCE_PROVIDER;
        metadataJdbcRepository.jsonSetter = new HsqlPostgresJsonSetter();

        final Liquibase liquibase = new Liquibase(
                LIQUIBASE_FILE_STORE_DB_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(),
                new JdbcConnection(DATA_SOURCE_PROVIDER.getDataSource().getConnection()));
        liquibase.dropAll();
        liquibase.update("");
    }

    @Test
    public void shouldStoreAndRetrieveFileData() {

        final UUID fileId = randomUUID();
        final byte[] content = "file-name".getBytes();
        fileJdbcRepository.insert(fileId, content);

        final Optional<byte[]> fileContents = fileJdbcRepository.findByFileId(fileId);

        assertThat(fileContents.isPresent(), is(true));
        assertThat(fileContents.get(), is(content));
    }

    @Test
    public void shouldStoreAndRetrieveMetadata() {

        final UUID fileId = randomUUID();

        final String json = "{\"some\": \"json\"}";
        final byte[] content = "some file or other".getBytes();
        final JsonObject metadata = toJsonObject(json);

        fileJdbcRepository.insert(fileId, content);
        metadataJdbcRepository.insert(fileId, metadata);

        final Optional<JsonObject> foundMetadata = metadataJdbcRepository.findByFileId(fileId);

        assertThat(foundMetadata.isPresent(), is(true));
        assertThat(foundMetadata.get(), is(metadata));
    }

    private JsonObject toJsonObject(final String json) {
        return createReader(new StringReader(json)).readObject();
    }
}
