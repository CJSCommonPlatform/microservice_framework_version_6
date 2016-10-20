package uk.gov.justice.services.example.cakeshop.query.controller;

import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import org.junit.Test;

public class CakeOrdersQueryControllerTest {

    @Test
    public void shouldHandleCakeOrdersQuery() throws Exception {
        assertThat(CakeOrdersQueryController.class, isHandlerClass(QUERY_CONTROLLER)
                .with(method("getOrder")
                        .thatHandles("example.get-order")
                        .withRequesterPassThrough()));
    }
}