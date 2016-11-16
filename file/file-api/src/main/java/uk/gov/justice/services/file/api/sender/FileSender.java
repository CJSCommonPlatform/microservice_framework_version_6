package uk.gov.justice.services.file.api.sender;

public interface FileSender {

    FileData send(final String fileName, final byte[] content);

}
