package uk.gov.justice.services.adapter.rest.parameter;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.raml.model.ParamType;

/**
 * Enum containing types of http query parameters
 */
public enum ParameterType {
    STRING, NUMERIC, BOOLEAN;
    /**
     * Converts RAML parameter type to {@link ParameterType}
     * @param ramlParamType RAML parameter type
     * @return a matching {@link ParameterType}
     */
    public static ParameterType valueOf(final ParamType ramlParamType) {
        return PARAM_MAP.getOrDefault(ramlParamType, ParameterType.STRING);
    }

    private static final Map<ParamType, ParameterType> PARAM_MAP =
            ImmutableMap.<ParamType, ParameterType>builder()
                    .put(ParamType.STRING, ParameterType.STRING)
                    .put(ParamType.NUMBER, ParameterType.NUMERIC)
                    .put(ParamType.INTEGER, ParameterType.NUMERIC)
                    .put(ParamType.BOOLEAN, ParameterType.BOOLEAN)
                    .build();
}