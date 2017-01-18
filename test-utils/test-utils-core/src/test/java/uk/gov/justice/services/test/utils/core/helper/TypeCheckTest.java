package uk.gov.justice.services.test.utils.core.helper;

import static org.hamcrest.CoreMatchers.equalTo;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.Times.times;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.typeCheck;

import uk.gov.justice.services.test.utils.core.random.Generator;

import org.junit.Test;

/**
 * Unit tests for the {@link TypeCheck} class.
 */
public class TypeCheckTest {

    @Test
    public void shouldPassIfConditionIsAlwaysTrue() {
        typeCheck(new SingleIntegerGenerator(), i -> i == 1).verify(times(10));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfConditionIsEverFalse() {
        typeCheck(new SingleIntegerGenerator(), i -> i != 1).verify();
    }

    @Test
    public void shouldPassIfMatcherPasses() {
        typeCheck(new SingleIntegerGenerator(), equalTo(1)).verify(times(10));
    }

    private class SingleIntegerGenerator extends Generator<Integer> {
        @Override
        public Integer next() {
            return 1;
        }
    }

}
