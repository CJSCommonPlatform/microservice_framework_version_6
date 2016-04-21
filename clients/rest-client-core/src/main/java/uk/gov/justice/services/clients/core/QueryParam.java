package uk.gov.justice.services.clients.core;

/**
 * Class representing a query parameter for a REST endpoint.
 */
public class QueryParam {

    private final String name;
    private final boolean required;

    /**
     * Constructor.
     * @param name the name of the parameter
     * @param required true if the parameter is required
     */
    public QueryParam(final String name, final boolean required) {
        this.name = name;
        this.required = required;
    }

    /**
     * Get the name of this parameter.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Is this query parameter required
     * @return true if this parameter is mandatory
     */
    public boolean isRequired() {
        return required;
    }
}
