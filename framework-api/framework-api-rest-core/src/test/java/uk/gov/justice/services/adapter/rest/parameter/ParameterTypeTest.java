package uk.gov.justice.services.adapter.rest.parameter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParameterTypeTest {

    @Test
    public void shouldReturnMappedTypes() throws Exception {
        assertEquals(ParameterType.valueOfQueryType("STRING"), ParameterType.STRING);
        assertEquals(ParameterType.valueOfQueryType("NUMBER"), ParameterType.NUMERIC);
        assertEquals(ParameterType.valueOfQueryType("INTEGER"), ParameterType.NUMERIC);
        assertEquals(ParameterType.valueOfQueryType("BOOLEAN"), ParameterType.BOOLEAN);
    }

    @Test
    public void shouldReturnStringIfTypeIsUnmapped() {
        assertEquals(ParameterType.valueOfQueryType("DATE"), ParameterType.STRING);
    }
}