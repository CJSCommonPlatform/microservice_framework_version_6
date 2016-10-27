package uk.gov.justice.services.adapter.rest.parameter;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.rest.ParameterType;

import java.math.BigDecimal;
import java.util.Collection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ValidParameterCollectionBuilderTest {

    private ValidParameterCollectionBuilder validParameterCollectionBuilder;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        validParameterCollectionBuilder = new ValidParameterCollectionBuilder();
    }

    @Test
    public void shouldReturnEmptyMap() throws Exception {
        assertThat(validParameterCollectionBuilder.parameters().size(), is(0));
    }

    @Test
    public void shouldReturnMapWithRequiredParameter() throws Exception {
        validParameterCollectionBuilder.putRequired("Name1", "Value1", ParameterType.STRING);

        Collection<Parameter> validParameters = validParameterCollectionBuilder.parameters();

        assertThat(validParameters.size(), is(1));
        assertThat(validParameters.iterator().next().getStringValue(), is("Value1"));
    }

    @Test
    public void shouldReturnMapWithNoOptionalParameterIfNullValue() throws Exception {
        validParameterCollectionBuilder.putOptional("OptionalName1", null, ParameterType.STRING);
        Collection<Parameter> validParameters = validParameterCollectionBuilder.parameters();
        assertThat(validParameters.size(), is(0));
    }

    @Test
    public void shouldReturnMapWithOptionalParameterIfValueSet() throws Exception {
        validParameterCollectionBuilder.putOptional("OptionalName1", "OptionalValue1", ParameterType.STRING);

        Collection<Parameter> validParameters = validParameterCollectionBuilder.parameters();

        assertThat(validParameters.size(), is(1));
        assertThat(validParameters.iterator().next().getStringValue(), is("OptionalValue1"));
    }

    @Test
    public void shouldReturnMapWithMultipleRequiredAndOptionalParameters() throws Exception {
        validParameterCollectionBuilder
                .putRequired("paramName1", "Value1", ParameterType.STRING)
                .putOptional("OptionalName1", null, ParameterType.STRING)
                .putOptional("OptionalName2", "OptionalValue2", ParameterType.STRING)
                .putOptional("OptionalName3", "1111", ParameterType.NUMERIC)
                .putRequired("Name2", "567", ParameterType.NUMERIC);

        Collection<Parameter> validParameters = validParameterCollectionBuilder.parameters();

        assertThat(validParameters, hasSize(4));
        assertThat(validParameters, hasItems(
                allOf(hasProperty("name", equalTo("paramName1")), hasProperty("stringValue", equalTo("Value1"))),
                allOf(hasProperty("name", equalTo("OptionalName2")), hasProperty("stringValue", equalTo("OptionalValue2"))),
                allOf(hasProperty("name", equalTo("OptionalName3")), hasProperty("numericValue", equalTo(BigDecimal.valueOf(1111)))),
                allOf(hasProperty("name", equalTo("Name2")), hasProperty("numericValue", equalTo(BigDecimal.valueOf(567))))
        ));

    }

    @Test
    public void shouldThrowExceptionIfRequiredParameterHasNullValue() throws Exception {
        exception.expect(BadRequestException.class);
        exception.expectMessage("The required parameter Name1 has no value.");

        validParameterCollectionBuilder
                .putRequired("Name1", null, ParameterType.STRING)
                .parameters();
    }

    @Test
    public void shouldThrowExceptionInCaseOfInvalidNumericParamValue() throws Exception {
        exception.expect(BadRequestException.class);
        exception.expectMessage("Invalid parameter value.");

        validParameterCollectionBuilder
                .putRequired("param", "NonNumeric", ParameterType.NUMERIC)
                .parameters();
    }

    @Test
    public void shouldThrowExceptionInCaseOfInvalidNumericParamValue2() throws Exception {
        exception.expect(BadRequestException.class);
        exception.expectMessage("Invalid parameter value.");

        validParameterCollectionBuilder
                .putOptional("param", "NonNumeric", ParameterType.NUMERIC)
                .parameters();
    }

    @Test
    public void shouldThrowExceptionInCaseOfInvalidBooleanParamValue() throws Exception {
        exception.expect(BadRequestException.class);
        exception.expectMessage("Invalid parameter value.");

        validParameterCollectionBuilder
                .putRequired("param", "NonBoolean", ParameterType.BOOLEAN)
                .parameters();
    }

    @Test
    public void shouldThrowExceptionInCaseOfInvalidBooleanParamValue2() throws Exception {
        exception.expect(BadRequestException.class);
        exception.expectMessage("Invalid parameter value.");

        validParameterCollectionBuilder
                .putOptional("param", "NonBoolean", ParameterType.BOOLEAN)
                .parameters();
    }

}