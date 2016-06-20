package uk.gov.justice.services.generators.commons.helper;

import static org.apache.commons.lang3.StringUtils.stripStart;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessagingResourceUri {
    private static final Pattern MESSAGING_RESOURCE_URI_PATTERN =
            Pattern.compile("^\\/?([a-zA-Z0-9]+)\\.(((api|controller|handler)\\.(command))|(event))");

    private final String uriString;
    private final String tier;
    private final String context;
    private final String pillar;

    public MessagingResourceUri(String uriString) {
        this.uriString = uriString;
        final Matcher m = matcherOf(this.uriString);
        m.find();
        this.context = m.group(1);
        this.tier = m.group(4);
        this.pillar = m.group(5) != null ? m.group(5) : m.group(6);
    }

    private static Matcher matcherOf(final String uriString) {
        return MESSAGING_RESOURCE_URI_PATTERN.matcher(uriString);
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

    /**
     * @return component tier
     */
    public String tier() {
        return tier;
    }

    /**
     * @return component pillar
     */
    public String pillar() {
        return pillar;
    }

    /**
     * @return service context name
     */
    public String context() {
        return context;
    }

    /**
     * Constructs the messaging destination name from the URI
     *
     * @return messaging destination name
     */
    public String destinationName() {
        return stripStart(uriString, "/");
    }

    @Override
    public String toString() {
        return uriString;
    }

}
