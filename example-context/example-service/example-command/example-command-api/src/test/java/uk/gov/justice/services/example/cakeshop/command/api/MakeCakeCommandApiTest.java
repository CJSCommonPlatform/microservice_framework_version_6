package uk.gov.justice.services.example.cakeshop.command.api;


import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import org.junit.Test;

public class MakeCakeCommandApiTest {

    @Test
    public void shouldHandleMakeCakeCommand() throws Exception {
        assertThat(MakeCakeCommandApi.class, isHandlerClass(COMMAND_API)
                .with(method("handle")
                        .thatHandles("example.make-cake")
                        .withSenderPassThrough()));
    }
}
