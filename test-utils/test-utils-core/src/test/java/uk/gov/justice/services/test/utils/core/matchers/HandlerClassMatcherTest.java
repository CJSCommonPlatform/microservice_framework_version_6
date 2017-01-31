package uk.gov.justice.services.test.utils.core.matchers;

import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isCustomHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.matchers.ServiceComponentTestClasses.CUSTOM_API;

import uk.gov.justice.services.test.utils.core.matchers.ServiceComponentTestClasses.NoHandlerMethod;
import uk.gov.justice.services.test.utils.core.matchers.ServiceComponentTestClasses.NoServiceComponentAnnotation;
import uk.gov.justice.services.test.utils.core.matchers.ServiceComponentTestClasses.ValidCommandApi;
import uk.gov.justice.services.test.utils.core.matchers.ServiceComponentTestClasses.ValidCustomServiceComponent;

import org.junit.Test;

public class HandlerClassMatcherTest {

    @Test
    public void shouldMatchIfServiceComponentAnnotation() throws Exception {
        assertThat(ValidCommandApi.class, isHandlerClass(COMMAND_API));
    }

    @Test
    public void shouldMatchIfCustomServiceComponentAnnotation() throws Exception {
        assertThat(ValidCustomServiceComponent.class, isCustomHandlerClass(CUSTOM_API));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfNoServiceComponentAnnotation() throws Exception {
        assertThat(NoServiceComponentAnnotation.class, isHandlerClass(COMMAND_API));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchWhenNoAnnotationGiven() throws Exception {
        assertThat(ValidCustomServiceComponent.class, isCustomHandlerClass(null));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfNoCustomServiceComponentAnnotation() throws Exception {
        assertThat(NoServiceComponentAnnotation.class, isCustomHandlerClass(CUSTOM_API));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchInstanceOfAHandlerWithTheWrongAnnotation() throws Exception {
        assertThat(ValidCommandApi.class, isHandler(QUERY_API));
    }

    @Test
    public void shouldMatchHandlerMethod() throws Exception {
        assertThat(ValidCommandApi.class, isHandlerClass(COMMAND_API).with(method("testA").thatHandles("context.commandA")));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchWhenNoHandlerMethod() throws Exception {
        assertThat(NoHandlerMethod.class, isHandlerClass(COMMAND_API).with(method("testA").thatHandles("context.commandA")));
    }
}
