package uk.gov.justice.services.example.cakeshop.query.controller;

import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import org.junit.Test;

public class CakesQueryControllerTest {
    @Test
    public void shouldBeQueryApiThatHasRequesterPassThroughMethod() throws Exception {
        assertThat(CakesQueryController.class, isHandlerClass(QUERY_CONTROLLER)
                .with(method("cakes")
                        .thatHandles("example.search-cakes")
                        .withRequesterPassThrough()
                ));
    }
}
