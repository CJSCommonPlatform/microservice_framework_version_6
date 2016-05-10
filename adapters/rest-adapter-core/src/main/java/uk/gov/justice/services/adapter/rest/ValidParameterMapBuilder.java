package uk.gov.justice.services.adapter.rest;

import static java.lang.String.format;
import static uk.gov.justice.services.adapter.rest.ValidParameterMapBuilder.ParameterType.OPTIONAL;
import static uk.gov.justice.services.adapter.rest.ValidParameterMapBuilder.ParameterType.REQUIRED;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableMap;

/**
 * Validates added query and path parameters and builds an {@link ImmutableMap}.
 *
 * Optional query parameters that are set to null are filtered and not added to the valid map.
 * Required query parameters and path parameters if set to null will throw a {@link
 * BadRequestException}. Repeated parameters names will throw a {@link BadRequestException}.
 */
public class ValidParameterMapBuilder {

    private final Collection<Parameter> parameters = new ArrayList<>();

    /**
     * Validate the optional and required parameters, build and return map of all valid parameters.
     * Throws a BadRequestException if a required parameter value is null.
     *
     * @return the map that contains the valid parameters
     * @throws BadRequestException if a required parameter value is null
     */
    public Map<String, String> validateAndBuildMap() {
        final ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();

        parameters.stream()
                .filter(removeOptionalsThatAreNull())
                .forEach(parameter -> {
                    validateRequiredParameterIsNotNull(parameter);
                    mapBuilder.put(parameter.name, parameter.value);
                });

        try {
            return mapBuilder.build();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    /**
     * Add a required parameter to the parameter list.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return the current instance of {@link ValidParameterMapBuilder}
     */
    public ValidParameterMapBuilder putRequired(final String name, final String value) {
        parameters.add(new Parameter(name, value, REQUIRED));
        return this;
    }

    /**
     * Add an optional parameter to the parameter list
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return the current instance of {@link ValidParameterMapBuilder}
     */
    public ValidParameterMapBuilder putOptional(final String name, final String value) {
        parameters.add(new Parameter(name, value, OPTIONAL));
        return this;
    }

    private Predicate<Parameter> removeOptionalsThatAreNull() {
        return parameter ->
                parameter.parameterType == REQUIRED ||
                        (parameter.parameterType == OPTIONAL && parameter.value != null);
    }

    private void validateRequiredParameterIsNotNull(final Parameter parameter) {
        if (parameter.parameterType == REQUIRED && parameter.value == null) {
            throw new BadRequestException(format("The required parameter %s has no value.", parameter.name));
        }
    }

    enum ParameterType {
        REQUIRED, OPTIONAL
    }

    private class Parameter {
        private final String name;
        private final String value;
        private final ParameterType parameterType;

        Parameter(final String name, final String value, final ParameterType parameterType) {
            this.name = name;
            this.value = value;
            this.parameterType = parameterType;
        }
    }
}
