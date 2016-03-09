package uk.gov.justice.services.messaging.context;


import uk.gov.justice.services.messaging.exception.InvalidNameException;

public final class ContextName {
    private ContextName() {
    }

    /**
     * Extracts context name from the logical action or event name.
     *
     * @param name logical name of the action or event.
     * @return context name
     */
    public static String fromName(final String name) {
        if (!name.contains(".")) {
            throw new InvalidNameException("Invalid action or event name " + name);
        } else return name.split("\\.")[0];
    }

}
