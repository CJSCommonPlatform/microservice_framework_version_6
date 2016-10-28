package uk.gov.justice.services.test.utils.core.matchers;

import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import uk.gov.justice.services.test.utils.core.matchers.ServiceComponentTestClasses.NoHandlerMethod;
import uk.gov.justice.services.test.utils.core.matchers.ServiceComponentTestClasses.NoServiceComponentAnnotation;
import uk.gov.justice.services.test.utils.core.matchers.ServiceComponentTestClasses.ValidCommandApi;

import org.junit.Test;

public class HandlerMatcherTest {

    @Test
    public void shouldMatchInstanceOfCommandApiHandlerWithAnnotation() throws Exception {
        assertThat(new ValidCommandApi(), isHandler(COMMAND_API));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchInstanceOfAHandlerWithNoAnnotation() throws Exception {
        assertThat(new NoServiceComponentAnnotation(), isHandler(COMMAND_API));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchInstanceOfAHandlerWithTheWrongAnnotation() throws Exception {
        assertThat(new ValidCommandApi(), isHandler(QUERY_API));
    }

    @Test
    public void shouldMatchWithGivenMatcherHandlerMethod() throws Exception {
        assertThat(new ValidCommandApi(), isHandler(COMMAND_API).with(method("testA").thatHandles("context.commandA")));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchWithGivenMatcher() throws Exception {
        assertThat(new NoHandlerMethod(), isHandler(COMMAND_API).with(method("testA").thatHandles("context.commandA")));
    }
}