package uk.gov.justice.services.core.util;

public final class CoreUtil {
    private CoreUtil() {
    }

    /**
     * Extracts context name from the logical action or event name.
     *
     * @param actionOrEventName logical name of the action or event.
     * @return context name
     */
    public static String extractContextNameFromActionOrEventName(final String actionOrEventName) {
        if (!actionOrEventName.contains(".")) {
            throw new IllegalArgumentException("Invalid action or event name: " + actionOrEventName);
        } else return actionOrEventName.split("\\.")[0];
    }

}
