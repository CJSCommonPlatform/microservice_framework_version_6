package uk.gov.justice.services.test.utils.core.matchers;

import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.matchers.MethodHandlesAnnotationMatcher.methodThatHandles;

import uk.gov.justice.services.core.annotation.Handles;

import org.junit.Test;

public class MethodHandlesAnnotationMatcherTest {

    @Test
    public void shouldMatchMethodWithHandlesAnnotation() throws Exception {
        assertThat(TestClass.class.getMethod("testA"), methodThatHandles("context.commandA"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchMethodWithNoHandlesAnnotation() throws Exception {
        assertThat(TestClass.class.getMethod("testB"), methodThatHandles("context.commandB"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchMethodWithDifferentHandlesValue() throws Exception {
        assertThat(TestClass.class.getMethod("testC"), methodThatHandles("context.commandC"));
    }

    public static class TestClass {
        @Handles("context.commandA")
        public void testA() {
        }

        public void testB() {
        }

        @Handles("context.commandA")
        public void testC() {
        }
    }
}