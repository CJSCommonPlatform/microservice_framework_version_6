package uk.gov.justice.services.adapter.rest.parameter;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.raml.model.ParamType;

public class ParameterTypeTest {

    @Test
    public void shouldReturnFrameworkParamTypesMappedToRamlTypes() throws Exception {
        assertThat(ParameterType.valueOfQueryType(ParamType.STRING.name()), is(ParameterType.STRING));
        assertThat(ParameterType.valueOfQueryType(ParamType.NUMBER.name()), is(ParameterType.NUMERIC));
        assertThat(ParameterType.valueOfQueryType(ParamType.INTEGER.name()), is(ParameterType.NUMERIC));
        assertThat(ParameterType.valueOfQueryType(ParamType.BOOLEAN.name()), is(ParameterType.BOOLEAN));
    }

    @Test
    public void shouldReturnStringIfTypeUnmapped() {
        assertThat(ParameterType.valueOfQueryType(ParamType.DATE.name()), is(ParameterType.STRING));
    }
}