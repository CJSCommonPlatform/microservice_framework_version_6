package uk.gov.justice.services.adapter.rest.parameter;

import java.util.Collection;

public interface ParameterCollectionBuilder {

    /**
     * returns collection of all valid parameters.
     *
     * @return the collection that contains the valid parameters
     */
    Collection<Parameter> parameters();

    /**
     * Add a required parameter to the parameter list.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @param type  the parameter type
     * @return the current instance of {@link ParameterCollectionBuilder}
     */
    ParameterCollectionBuilder putRequired(final String name, final String value, final ParameterType type);

    /**
     * Add an optional parameter to the parameter list
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @param type  the parameter type
     * @return the current instance of {@link ParameterCollectionBuilder}
     */
    ParameterCollectionBuilder putOptional(final String name, final String value, final ParameterType type);
}