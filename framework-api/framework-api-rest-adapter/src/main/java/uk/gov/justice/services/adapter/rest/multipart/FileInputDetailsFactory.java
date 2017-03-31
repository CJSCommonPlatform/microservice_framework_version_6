package uk.gov.justice.services.adapter.rest.multipart;

import java.util.List;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

public interface FileInputDetailsFactory {

    List<FileInputDetails> createFileInputDetailsFrom(final MultipartFormDataInput multipartFormDataInput, final List<String> fieldNames);
}