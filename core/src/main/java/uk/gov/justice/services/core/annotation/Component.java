package uk.gov.justice.services.core.annotation;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * Enum representing all the service components.
 */
public enum Component {

    COMMAND_API("command", "api", Queue.class),
    COMMAND_CONTROLLER("command", "controller", Queue.class),
    COMMAND_HANDLER("command", "handler", Queue.class),
    EVENT_LISTENER("event", "listener", Topic.class),
    EVENT_PROCESSOR("event", "processor", Topic.class),
    EVENT_API("event", "api", null),
    QUERY_API("query", "api", null),
    QUERY_CONTROLLER("query", "controller", null),
    QUERY_VIEW("query", "view", null);

    private final String pillar;
    private final String tier;
    private final Class<? extends Destination> inputDestinationType;

    Component(final String pillar, final String tier, final Class<? extends Destination> inputDestinationType) {
        this.pillar = pillar;
        this.tier = tier;
        this.inputDestinationType = inputDestinationType;
    }

    /**
     * Returns component of the provided pillar and tier.
     *
     * @param pillar the pillar
     * @param tier   the tier
     * @return the component for the provided pillar and tier
     */
    public static Component valueOf(final String pillar, final String tier) {
        return valuesStream()
                .filter(c -> c.pillar.equals(pillar) && c.tier.equals(tier))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        format("No enum constant for pillar: %s, tier: %s", pillar, tier)));
    }


    /**
     * Checks if enum contains component with given name
     *
     * @param name - component name
     * @return true if the enum contains the given name, false otherwise
     */
    public static boolean contains(final String name) {
        return valuesStream().filter(c -> c.name().equals(name)).findAny().isPresent();
    }

    private static Stream<Component> valuesStream() {
        return stream(Component.values());
    }

    public static String names(final String delimiter) {
        return valuesStream().map(Component::name).collect(joining(delimiter));
    }

    public String pillar() {
        return pillar;
    }

    public String tier() {
        return tier;
    }

    public Class<? extends Destination> inputDestinationType() {
        return inputDestinationType;
    }
}
