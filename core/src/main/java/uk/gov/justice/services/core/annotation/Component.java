package uk.gov.justice.services.core.annotation;

/**
 * Enum representing all the service components.
 */
public enum Component {

    COMMAND_API, COMMAND_CONTROLLER, COMMAND_HANDLER;

    /**
     * Retrieves the Component of the provided {@link ServiceComponent}
     *
     * @param clazz The service component to be analysed.
     * @return the component from the provided {@link ServiceComponent}.
     */
    public static Component getComponentFromServiceComponent(Class<ServiceComponent> clazz) {
        ServiceComponent serviceComponent = clazz.getAnnotation(ServiceComponent.class);
        return serviceComponent.value();
    }

    /**
     * Retrieves the Component of the provided {@link Adapter}
     *
     * @param clazz The adapter class to be analysed.
     * @return the component from the provided {@link Adapter}.
     */
    public static Component getComponentFromAdapter(final Class<Adapter> clazz) {
        Adapter adapter = clazz.getAnnotation(Adapter.class);
        return adapter.value();
    }

}
