package uk.gov.justice.services.core.annotation;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing all the service components.
 */
public final class Component {

    public static final String COMMAND_API = "COMMAND_API";
    public static final String COMMAND_CONTROLLER = "COMMAND_CONTROLLER";
    public static final String COMMAND_HANDLER = "COMMAND_HANDLER";

    public static final String EVENT_API = "EVENT_API";
    public static final String EVENT_PROCESSOR = "EVENT_PROCESSOR";
    public static final String EVENT_LISTENER = "EVENT_LISTENER";

    public static final String QUERY_API = "QUERY_API";
    public static final String QUERY_CONTROLLER = "QUERY_CONTROLLER";
    public static final String QUERY_VIEW = "QUERY_VIEW";

    private Component() {
    }

    private static final List<String> components = new ArrayList<String>() {

        private static final long serialVersionUID = -7403626856706763685L;

        {
            add(COMMAND_API);
            add(COMMAND_CONTROLLER);
            add(COMMAND_HANDLER);

            add(EVENT_API);
            add(EVENT_PROCESSOR);
            add(EVENT_LISTENER);

            add(QUERY_API);
            add(QUERY_CONTROLLER);
            add(QUERY_VIEW);
        }
    };

    /**
     * Returns component name of the provided pillar and tier.
     *
     * @param pillar the pillar
     * @param tier   the tier
     * @return the component name for the provided pillar and tier
     */
    public static String valueOf(final String pillar, final String tier) {
        final String pillarAndTier = (pillar + "_" + tier).toUpperCase();

        if (components.contains(pillarAndTier)) {
            return pillarAndTier;
        } else {
            throw new IllegalArgumentException(
                    format("No component matches pillar: %s, tier: %s", pillar, tier));
        }
    }

    /**
     * Checks if given name is a component
     *
     * @param name - component name
     * @return true if the given name is a component, false otherwise
     */
    public static boolean contains(final String name) {
        return components.contains(name);
    }
}