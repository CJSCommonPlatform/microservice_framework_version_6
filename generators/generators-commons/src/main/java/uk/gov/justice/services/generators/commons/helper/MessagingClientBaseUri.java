package uk.gov.justice.services.generators.commons.helper;

import static java.lang.String.format;
import static uk.gov.justice.services.generators.commons.helper.Names.buildJavaFriendlyName;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses RAML base URI for messaging client generation and exposes it's parts through accessor methods
 */
public class MessagingClientBaseUri {

    private static final Pattern MESSAGING_BASE_URI_PATTERN = Pattern.compile("message://(.+)");
    private final String uriExcludingProtocol;

    public MessagingClientBaseUri(final String uriString) {
        final Matcher m = matcherOf(uriString);
        if(m.find()) {
            this.uriExcludingProtocol = m.group(1);
        } else {
            throw new IllegalArgumentException(format("Base URI %s does not match message://(.+) pattern", uriString));
        }
    }

    /**
     * Checks if the uri adheres to framework's standard
     *
     * @param uriString - uri to validate
     * @return true is uri adheres to framework's standard, false otherwise
     */
    public static boolean valid(final String uriString) {
        return matcherOf(uriString).matches();
    }

    private static Matcher matcherOf(final String uriString) {
        return MESSAGING_BASE_URI_PATTERN.matcher(uriString);
    }

    /**
     * Returns a camel case class name with all invalid characters removed
     *
     * @return camel case string with all invalid characters removed.
     */
    public String toClassName() {
        return buildJavaFriendlyName(uriExcludingProtocol);
    }
}
