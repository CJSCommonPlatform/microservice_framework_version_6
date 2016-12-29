package uk.gov.justice.services.file.api;

import uk.gov.justice.services.file.api.domain.StorableFile;

import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

public interface FileServiceClient {

    void store(final StorableFile storableFile);
    Optional<StorableFile> find(final UUID fileId);
}
