package uk.gov.justice.services.core.extension;

/**
 * Event representing the occurrence of a class with an {@link uk.gov.justice.services.core.annotation.Provider}
 * annotation having been identified by the framework.
 */
public class ProviderFoundEvent {

    private final Class<?> clazz;

    public ProviderFoundEvent(final Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
