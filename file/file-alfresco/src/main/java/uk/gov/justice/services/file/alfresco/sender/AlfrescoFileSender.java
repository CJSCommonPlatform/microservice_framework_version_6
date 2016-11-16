package uk.gov.justice.services.file.alfresco.sender;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;

import uk.gov.justice.services.common.configuration.GlobalValue;
import uk.gov.justice.services.file.alfresco.rest.AlfrescoRestClient;
import uk.gov.justice.services.file.api.sender.FileData;
import uk.gov.justice.services.file.api.sender.FileSender;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class AlfrescoFileSender implements FileSender {

    private static final String CPPUID = "cppuid";


    @Inject
    @GlobalValue(key = "alfrescoUploadPath", defaultValue = "/service/case/upload")
    String alfrescoUploadPath;

    @Inject
    @GlobalValue(key = "alfrescoUploadUser")
    String alfrescoUploadUser;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    AlfrescoRestClient restClient;


    @Override
    public FileData send(final String fileName, final byte[] content) {
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.put(CPPUID, Collections.singletonList(alfrescoUploadUser));

        restClient.post(alfrescoUploadPath, MULTIPART_FORM_DATA_TYPE, headers, entity(content, MULTIPART_FORM_DATA_TYPE));

        //TODO: build response

        return null;
    }
}
