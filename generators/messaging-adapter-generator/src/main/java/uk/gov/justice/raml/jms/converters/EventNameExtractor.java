package uk.gov.justice.raml.jms.converters;

public class EventNameExtractor {

    private static final String MIME_TYPE_PREFIX = "application/vnd.";
    private static final String MIME_TYPE_SUFFIX = "+json";

    public String extractEventName(final String mimeTypeString)  {

        final int startIndex = MIME_TYPE_PREFIX.length();
        final int endIndex = mimeTypeString.length() - MIME_TYPE_SUFFIX.length();

        return mimeTypeString.substring(startIndex, endIndex);
    }
}
