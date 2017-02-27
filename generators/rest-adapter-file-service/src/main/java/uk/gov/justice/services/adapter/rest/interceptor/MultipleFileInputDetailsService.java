package uk.gov.justice.services.adapter.rest.interceptor;

import uk.gov.justice.services.adapter.rest.mutipart.FileInputDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MultipleFileInputDetailsService {

    @Inject
    FileInputDetailsHandler fileInputDetailsHandler;

    @SuppressWarnings("ConstantConditions")
    public Map<String, UUID> storeFileDetails(final List<FileInputDetails> fileInputDetails) {

        final Map<String, UUID> results = new HashMap<>();

        fileInputDetails
                .forEach(inputDetails -> {
                    final UUID fileId = fileInputDetailsHandler.store(inputDetails);
                    results.put(inputDetails.getFieldName(), fileId);
                });

        return results;
    }
}
