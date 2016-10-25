package uk.gov.justice.services.clients.core;

import uk.gov.justice.services.rest.ParameterType;

/**
 * Class representing a query parameter definition for a REST endpoint.
 */
public class QueryParam {

    private final String name;
    private final boolean required;
    private final ParameterType type;

    /**
     * Constructor.
     *  @param name     the name of the parameter
     * @param required true if the parameter is required
     * @param type
     */
    public QueryParam(final String name, final boolean required, final ParameterType type) {
        this.name = name;
        this.required = required;
        this.type = type;
    }

    /**
     * Get the name of this parameter.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Is this query parameter required
     *
     * @return true if this parameter is mandatory
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Get the type of this parameter.
     *
     * @return the type
     */
    public ParameterType getType() {
        return type;
    }
}
