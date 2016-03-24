package uk.gov.justice.services.core.annotation;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.Optional;

import static java.util.Arrays.stream;

/**
 * Enum representing all the service components.
 */
public enum Component {

    COMMAND_API("commands", "api", Queue.class),
    COMMAND_CONTROLLER("commands", "controller", Queue.class),
    COMMAND_HANDLER("commands", "handler", Queue.class),
    EVENT_LISTENER("events", "listener", Topic.class);

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
     * @param tier the tier
     * @return the component for the provided pillar and tier
     */
    public static Component valueOf(final String pillar, final String tier) {
        Optional<Component> first = stream(Component.values())
                .filter(c -> c.pillar.equals(pillar) && c.tier.equals(tier)).findFirst();

        return first.orElseThrow(() -> new IllegalArgumentException(
                String.format("No enum constant for pillar: %s, tier: %s", pillar, tier)));

    }

    /**
     * Retrieves the component of the provided {@link ServiceComponent}
     *
     * @param clazz The service component to be analysed.
     * @return the component from the provided {@link ServiceComponent}.
     */
    public static Component componentFromServiceComponent(final Class<?> clazz) {
        final ServiceComponent serviceComponent = clazz.getAnnotation(ServiceComponent.class);
        return serviceComponent.value();
    }

    /**
     * Retrieves the Component of the provided {@link Adapter}
     *
     * @param clazz The adapter class to be analysed.
     * @return the component from the provided {@link Adapter}.
     */
    public static Component componentFromAdapter(final Class<?> clazz) {
        final Adapter adapter = clazz.getAnnotation(Adapter.class);
        return adapter.value();
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
