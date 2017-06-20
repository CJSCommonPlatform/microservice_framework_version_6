package uk.gov.justice.services.generators.commons.helper;

import static org.apache.commons.lang3.StringUtils.stripStart;
import static org.apache.commons.lang3.text.WordUtils.capitalize;

public class MessagingResourceUri {

    private final String resourceUri;

    public MessagingResourceUri(final String resourceUri) {
        this.resourceUri = resourceUri;
    }

    /**
     * Constructs the messaging destination name from the URI
     *
     * @return messaging destination name
     */
    public String destinationName() {
        return stripStart(resourceUri.trim(), "/");
    }

    /**
     * Returns a camel case class name with all invalid characters removed
     *
     * @return camel case string with all invalid characters removed.
     */
    public String toClassName() {

        String cleaned = resourceUri.replaceAll("[^A-Za-z0-9_$]", " ");

        return capitalize(cleaned, " _$0123456789".toCharArray())
                .replaceAll(" ", "")
                .replaceAll("^[0-9]+", "");
    }

    public String hyphenated() {
        return destinationName().replaceAll("\\.", "-");
    }

    @Override
    public String toString() {
        return resourceUri;
    }
}
