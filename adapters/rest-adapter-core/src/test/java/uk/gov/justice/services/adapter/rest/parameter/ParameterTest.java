package uk.gov.justice.services.adapter.rest.parameter;


import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;

import org.junit.Test;

public class ParameterTest {

    @Test
    public void shouldCreateStringParameter() {
        Parameter param = Parameter.valueOf("paramName", "someStringValue", ParameterType.STRING);
        assertThat(param.getType(), is(ParameterType.STRING));
        assertThat(param.getName(), is("paramName"));
        assertThat(param.getStringValue(), is("someStringValue"));

    }

    @Test
    public void shouldCreateNumericParameterFromInt() {
        Parameter param = Parameter.valueOf("paramName2", "123", ParameterType.NUMERIC);
        assertThat(param.getType(), is(ParameterType.NUMERIC));
        assertThat(param.getName(), is("paramName2"));
        assertThat(param.getNumericValue(), is(BigDecimal.valueOf(123)));

    }

    @Test
    public void shouldCreateNumericParameterFromFloat() {
        Parameter param = Parameter.valueOf("paramName3", "123.02", ParameterType.NUMERIC);
        assertThat(param.getType(), is(ParameterType.NUMERIC));
        assertThat(param.getName(), is("paramName3"));
        assertThat(param.getNumericValue(), is(BigDecimal.valueOf(123.02)));
    }


    @Test
    public void shouldCreateBooleanParameter() {
        Parameter param = Parameter.valueOf("paramName4", "true", ParameterType.BOOLEAN);
        assertThat(param.getType(), is(ParameterType.BOOLEAN));
        assertThat(param.getName(), is("paramName4"));
        assertThat(param.getBooleanValue(), is(true));

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfInvalidNumericParamValuePassed() throws Exception {
        Parameter.valueOf("paramName3", "aaa", ParameterType.NUMERIC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfInvalidBooleanParamValuePassed() throws Exception {
        Parameter.valueOf("paramName3", "aaa", ParameterType.BOOLEAN);
    }

}