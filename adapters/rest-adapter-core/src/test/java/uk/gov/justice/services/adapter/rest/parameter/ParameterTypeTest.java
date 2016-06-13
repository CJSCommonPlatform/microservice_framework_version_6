package uk.gov.justice.services.adapter.rest.parameter;



import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.raml.model.ParamType;

public class ParameterTypeTest {

    @Test
    public void shouldReturnFrameworkParamTypesMappedToRamlTypes() throws Exception {
        assertThat(ParameterType.valueOf(ParamType.STRING), is(ParameterType.STRING));
        assertThat(ParameterType.valueOf(ParamType.NUMBER), is(ParameterType.NUMERIC));
        assertThat(ParameterType.valueOf(ParamType.INTEGER), is(ParameterType.NUMERIC));
        assertThat(ParameterType.valueOf(ParamType.BOOLEAN), is(ParameterType.BOOLEAN));
    }

    @Test
    public void shouldReturnStringIfTypeUnmapped() {
        assertThat(ParameterType.valueOf(ParamType.DATE), is(ParameterType.STRING));
    }
}