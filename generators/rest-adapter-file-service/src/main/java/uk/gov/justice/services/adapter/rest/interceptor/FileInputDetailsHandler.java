package uk.gov.justice.services.adapter.rest.interceptor;

import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.services.adapter.rest.multipart.FileInputDetails;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

@ApplicationScoped
public class FileInputDetailsHandler {

    @Inject
    SingleFileInputDetailsService singleFileInputDetailsService;

    public UUID store(final FileInputDetails fileInputDetails) {

        final String fileName = fileInputDetails.getFileName();

        final JsonObject metadata = createObjectBuilder()
                .add("fileName", fileName)
                .build();

        return singleFileInputDetailsService.store(fileInputDetails, metadata);
    }
}
