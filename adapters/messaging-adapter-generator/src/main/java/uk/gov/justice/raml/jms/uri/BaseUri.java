package uk.gov.justice.raml.jms.uri;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * Parses Raml base uri and exposes it's parts through accessor methods
 */
public class BaseUri {
    private static final Pattern MESSAGING_BASE_URI_PATTERN
            = Pattern.compile("message://(event|command|query)/(api|controller|handler|listener|processor)/\\S+/(\\S+)");
    private final String uriString;
    private final String tier;
    private final String pillar;
    private final String service;

    public BaseUri(final String uriString) {
        this.uriString = uriString;
        final Matcher m = matcherOf(uriString);
        m.find();
        this.pillar = m.group(1);
        this.tier = m.group(2);
        this.service = m.group(3);
    }

    /**
     * @return framework component tier
     */
    public String tier() {
        return tier;
    }

    /**
     * @return framework component pillar
     */
    public String pillar() {
        return pillar;
    }

    /**
     * @return application service name
     */
    public String service() {
        return service;
    }


    /**
     * Constructs clientId used in durable JMS subscribers
     *
     * @return messaging adapter clientId
     */
    public String adapterClientId() {
        return format("%s.%s.%s", service(), pillar(), tier());
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
}
