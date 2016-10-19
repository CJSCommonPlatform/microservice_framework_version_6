package uk.gov.justice.services.generators.commons.helper;

import static org.apache.commons.lang3.StringUtils.stripStart;
import static org.apache.commons.lang3.text.WordUtils.capitalize;

public class MessagingResourceUri {

    private final String uriString;

    public MessagingResourceUri(String uriString) {
        this.uriString = uriString;
    }

    /**
     * Constructs the messaging destination name from the URI
     *
     * @return messaging destination name
     */
    public String destinationName() {
        return stripStart(uriString, "/");
    }

    /**
     * Returns a camel case class name with all invalid characters removed
     *
     * @return camel case string with all invalid characters removed.
     */
    public String toClassName() {

        String cleaned = uriString.replaceAll("[^A-Za-z0-9_$]", " ");
        
        return capitalize(cleaned, " _$0123456789".toCharArray())
                .replaceAll(" ", "")
                .replaceAll("^[0-9]+", "");
    }

    @Override
    public String toString() {
        return uriString;
    }
}
