package uk.gov.justice.services.adapter.rest;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ValidParameterMapBuilderTest {

    private static final String PARAM_NAME_1 = "Name1";
    private static final String PARAM_VALUE_1 = "Value1";
    private static final String PARAM_NAME_2 = "Name2";
    private static final String PARAM_VALUE_2 = "Value2";
    private static final String OPTIONAL_PARAM_NAME_1 = "OptionalName1";
    private static final String OPTIONAL_PARAM_VALUE_1 = "OptionalValue1";
    private static final String OPTIONAL_PARAM_NAME_2 = "OptionalName2";
    private static final String OPTIONAL_PARAM_VALUE_2 = "OptionalValue2";
    private static final String NULL_VALUE = null;
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private ValidParameterMapBuilder validParameterMapBuilder;

    @Before
    public void setup() {
        validParameterMapBuilder = new ValidParameterMapBuilder();
    }

    @Test
    public void shouldReturnEmptyMap() throws Exception {
        assertThat(validParameterMapBuilder.validateAndBuildMap().size(), is(0));
    }

    @Test
    public void shouldReturnMapWithRequiredParameter() throws Exception {
        validParameterMapBuilder.putRequired(PARAM_NAME_1, PARAM_VALUE_1);

        Map<String, String> validParameters = validParameterMapBuilder.validateAndBuildMap();

        assertThat(validParameters.size(), is(1));
        assertThat(validParameters.get(PARAM_NAME_1), is(PARAM_VALUE_1));
    }

    @Test
    public void shouldReturnMapWithNoOptionalParameterIfNullValue() throws Exception {
        validParameterMapBuilder.putOptional(OPTIONAL_PARAM_NAME_1, NULL_VALUE);
        Map<String, String> validParameters = validParameterMapBuilder.validateAndBuildMap();
        assertThat(validParameters.size(), is(0));
    }

    @Test
    public void shouldReturnMapWithOptionalParameterIfValueSet() throws Exception {
        validParameterMapBuilder.putOptional(OPTIONAL_PARAM_NAME_1, OPTIONAL_PARAM_VALUE_1);

        Map<String, String> validParameters = validParameterMapBuilder.validateAndBuildMap();

        assertThat(validParameters.size(), is(1));
        assertThat(validParameters.get(OPTIONAL_PARAM_NAME_1), is(OPTIONAL_PARAM_VALUE_1));
    }

    @Test
    public void shouldReturnMapWithMultipleRequiredAndOptionalParameters() throws Exception {
        validParameterMapBuilder
                .putRequired(PARAM_NAME_1, PARAM_VALUE_1)
                .putOptional(OPTIONAL_PARAM_NAME_1, NULL_VALUE)
                .putOptional(OPTIONAL_PARAM_NAME_2, OPTIONAL_PARAM_VALUE_2)
                .putRequired(PARAM_NAME_2, PARAM_VALUE_2);

        Map<String, String> validParameters = validParameterMapBuilder.validateAndBuildMap();

        assertThat(validParameters.size(), is(3));
        assertThat(validParameters.get(PARAM_NAME_1), is(PARAM_VALUE_1));
        assertThat(validParameters.get(PARAM_NAME_2), is(PARAM_VALUE_2));
        assertThat(validParameters.get(OPTIONAL_PARAM_NAME_2), is(OPTIONAL_PARAM_VALUE_2));
    }

    @Test
    public void shouldThrowExceptionIfRequiredParameterHasNullValue() throws Exception {
        exception.expect(BadRequestException.class);
        exception.expectMessage(String.format("The required parameter %s has no value.", PARAM_NAME_1));

        validParameterMapBuilder
                .putRequired(PARAM_NAME_1, NULL_VALUE)
                .validateAndBuildMap();
    }

    @Test
    public void shouldThrowExceptionIfSameParameterNameIsAddedTwice() throws Exception {
        exception.expect(BadRequestException.class);
        exception.expectMessage(String.format("Multiple entries with same key: %s=%s and %s=%s",
                PARAM_NAME_1, PARAM_VALUE_2, PARAM_NAME_1, PARAM_VALUE_1));

        validParameterMapBuilder
                .putRequired(PARAM_NAME_1, PARAM_VALUE_1)
                .putRequired(PARAM_NAME_1, PARAM_VALUE_2)
                .validateAndBuildMap();
    }

}