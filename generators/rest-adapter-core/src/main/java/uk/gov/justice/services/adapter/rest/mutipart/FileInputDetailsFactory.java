package uk.gov.justice.services.adapter.rest.mutipart;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.adapter.rest.interceptor.FileStoreFailedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@ApplicationScoped
public class FileInputDetailsFactory {

    @Inject
    InputPartFileNameExtractor inputPartFileNameExtractor;

    public List<FileInputDetails> createFileInputDetailsFrom(final MultipartFormDataInput multipartFormDataInput, final List<String> fieldNames) {
        final Map<String, List<InputPart>> formDataMap = multipartFormDataInput.getFormDataMap();

        return fieldNames
                .stream()
                .map(fieldName -> fileInputDetailsFrom(formDataMap, fieldName))
                .collect(toList());
    }

    private FileInputDetails fileInputDetailsFrom(final Map<String, List<InputPart>> formDataMap, final String fieldName) {

        if(! formDataMap.containsKey(fieldName)) {
            throw new BadRequestException(format("Failed to find input part named '%s' as specified in the raml", fieldName));
        }

        final List<InputPart> inputParts = formDataMap.get(fieldName);

        if (inputParts.isEmpty()) {
            throw new BadRequestException(format("The list of input parts named '%s' is empty", fieldName));
        }

        final InputPart inputPart = inputParts.get(0);

        final String fileName = inputPartFileNameExtractor.extractFileName(inputPart);

        return new FileInputDetails(
                fileName,
                fieldName,
                inputStreamFrom(inputPart, fileName));
    }

    private InputStream inputStreamFrom(final InputPart inputPart, final String fileName) {
        try {
            return inputPart.getBody(InputStream.class, null);
        } catch (final IOException e) {
            throw new FileStoreFailedException(format("Failed to store file '%s'", fileName), e);
        }
    }
}
