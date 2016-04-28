package uk.gov.justice.services.core.annotation;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

import javax.enterprise.inject.spi.InjectionPoint;
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
    QUERY_API("query", "api", null),
    QUERY_CONTROLLER("query", "controller", null),
    QUERY_VIEW("query", "view", null);

    private final String pillar;
    private final String tier;
    private final Class<? extends Destination> destinationType;

    Component(final String pillar, final String tier, final Class<? extends Destination> destinationType) {
        this.pillar = pillar;
        this.tier = tier;
        this.destinationType = destinationType;
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
     * Retrieves the component of the provided {@link ServiceComponent} or {@link Adapter}.
     *
     * @param clazz The service component to be analysed
     * @return the component from the provided {@link ServiceComponent} or {@link Adapter}
     */
    public static Component componentFrom(final Class<?> clazz) {
        if (clazz.isAnnotationPresent(ServiceComponent.class)) {
            return clazz.getAnnotation(ServiceComponent.class).value();
        } else if (clazz.isAnnotationPresent(Adapter.class)) {
            return clazz.getAnnotation(Adapter.class).value();
        } else {
            throw new IllegalStateException(format("No annotation found to define component for class %s", clazz));
        }
    }

    /**
     * Retrieves the component of the provided injection point.
     *
     * @param injectionPoint the injection point to be analysed
     * @return the component from the provided injection point
     */
    public static Component componentFrom(final InjectionPoint injectionPoint) {
        return componentFrom(injectionPoint.getMember().getDeclaringClass());
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

    public Class<? extends Destination> destinationType() {
        return destinationType;
    }
}
