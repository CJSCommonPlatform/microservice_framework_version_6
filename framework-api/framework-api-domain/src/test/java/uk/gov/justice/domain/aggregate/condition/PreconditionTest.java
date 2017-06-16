package uk.gov.justice.domain.aggregate.condition;

import static uk.gov.justice.domain.aggregate.condition.Precondition.assertPrecondition;

import org.junit.Test;

/**
 * Unit tests for the {@link Precondition} class.
 */
public class PreconditionTest {

    @Test
    public void shouldSucceedForRawAssertion() {
        assertPrecondition(true).orElseThrow("Test");
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionForFailingRawAssertion() {
        assertPrecondition(false).orElseThrow("Test");
    }

    @Test
    public void shouldSucceedForSupplierAssertion() {
        assertPrecondition(() -> true).orElseThrow("Test");
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionForFailingSupplierAssertion() {
        assertPrecondition(() -> false).orElseThrow("Test");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowSupplierExceptionForFailingRawAssertion() {
        assertPrecondition(false).orElseThrow(() -> new IllegalStateException("What?!"));
    }
}
