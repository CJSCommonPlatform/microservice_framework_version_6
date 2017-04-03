package uk.gov.justice.services.adapter.rest.parameter;

/**
 * Enum containing types of HTTP query parameter types
 */
public enum ParameterType {

    /**
     * The supported types
     */
    STRING, NUMERIC, BOOLEAN;

    /**
     * Converts RAML parameter type to {@link ParameterType}
     *
     * @param ramlParamType RAML parameter type
     * @return a matching {@link ParameterType}
     */
    public static ParameterType valueOfQueryType(final String ramlParamType) {
        switch (ramlParamType) {
            case "STRING":
                return STRING;
            case "NUMBER":
                return NUMERIC;
            case "INTEGER":
                return NUMERIC;
            case "BOOLEAN":
                return BOOLEAN;
            default:
                return STRING;
        }
    }
}