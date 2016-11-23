package uk.gov.justice.services.file.alfresco.sender;

import static com.jayway.jsonpath.Configuration.defaultConfiguration;
import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.justice.services.file.alfresco.common.Headers.headersWithUserId;

import uk.gov.justice.services.common.configuration.GlobalValue;
import uk.gov.justice.services.file.alfresco.common.AlfrescoRestClient;
import uk.gov.justice.services.file.api.FileOperationException;
import uk.gov.justice.services.file.api.sender.FileData;
import uk.gov.justice.services.file.api.sender.FileSender;

import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import com.jayway.jsonpath.JsonPath;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;

@ApplicationScoped
public class AlfrescoFileSender implements FileSender {

    private static final String FORM_FILED_FILEDATA = "filedata";

    @Inject
    @GlobalValue(key = "alfrescoUploadPath", defaultValue = "/service/case/upload")
    String alfrescoUploadPath;

    @Inject
    @GlobalValue(key = "alfrescoUploadUser")
    String alfrescoUploadUser;

    @Inject
    AlfrescoRestClient restClient;

    @Override
    public FileData send(final String fileName, final InputStream content) {
        try {

            final Response response = restClient
                    .post(alfrescoUploadPath, MULTIPART_FORM_DATA_TYPE, headersWithUserId(alfrescoUploadUser), requestEntityOf(fileName, content));
            final String responseEntity = response.readEntity(String.class);

            if (response.getStatusInfo() != OK || isEmpty(responseEntity)) {
                //Alfresco is *very* accepting - failed exceptions represent service outages/problems only.
                throw new FileOperationException(format("Error while uploading document. Code:%d, Reason:%s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
            }
            return fileDataFrom(responseEntity);
        } catch (ProcessingException e) {
            throw new FileOperationException("Error uploading resource into Alfresco", e);
        }
    }

    private Entity<MultipartFormDataOutput> requestEntityOf(final String fileName, final InputStream content) {
        final MultipartFormDataOutput multipartFormDataOutput = new MultipartFormDataOutput();
        multipartFormDataOutput.addFormData(FORM_FILED_FILEDATA, content, TEXT_PLAIN_TYPE, fileName);

        return entity(multipartFormDataOutput, MULTIPART_FORM_DATA_TYPE);
    }

    private FileData fileDataFrom(final String responseEntity) {
        final Object responseDocument = defaultConfiguration().jsonProvider().parse(responseEntity);
        final String alfrescoAssetId = JsonPath.read(responseDocument, "$.nodeRef").toString().replace("workspace://SpacesStore/", "");
        final String mimeType = JsonPath.read(responseDocument, "$.fileMimeType");
        return new FileData(alfrescoAssetId, mimeType);
    }
}