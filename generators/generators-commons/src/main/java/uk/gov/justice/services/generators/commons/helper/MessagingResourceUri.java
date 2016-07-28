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
     * Returns capitalised uri string without delimiters
     *
     * @return capitalised uri string without delimiters
     */
    public String toCapitalisedString() {
        return capitalize(stripStart(uriString, "/"), '.').replace(".", "");
    }

    @Override
    public String toString() {
        return uriString;
    }
}
