package uk.gov.justice.services.adapter.rest.parameter;

import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;

import java.math.BigDecimal;

/**
 * Query or path parameter
 */
public class Parameter {
    private static final String INVALID_BOOLEAN_VALUE = "Invalid boolean value";
    private final String name;
    private final Object value;
    private final ParameterType type;


    /**
     * Builds parameter out of given arguments
     *
     * @param name  parameter name
     * @param value string representation of parameter value
     * @param type  parameter type
     * @return the parameter
     * @throws IllegalArgumentException - if parameter value string representation is invalid for
     *                                  the given value
     */
    public static Parameter valueOf(final String name, final String value, final ParameterType type) {
        return new Parameter(name, valueFromString(value, type), type);
    }

    private static Object valueFromString(final String value, final ParameterType type) {
        switch (type) {
            case NUMERIC:
                return new BigDecimal(value);
            case BOOLEAN:
                final Boolean booleanValue = toBooleanObject(value);
                if (booleanValue != null) {
                    return booleanValue;
                } else {
                    throw new IllegalArgumentException(INVALID_BOOLEAN_VALUE);
                }
            default:
                return value;
        }
    }

    private Parameter(final String name, final Object value, final ParameterType type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public ParameterType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getStringValue() {
        return (String) value;
    }

    public BigDecimal getNumericValue() {
        return (BigDecimal) value;
    }

    public Boolean getBooleanValue() {
        return (Boolean) value;
    }
}
