package uk.gov.justice.services.example.cakeshop.command.api;


import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import org.junit.Test;

public class AddRecipeCommandApiTest {

    @Test
    public void shouldHandleAddRecipeCommand() throws Exception {
        assertThat(AddRecipeCommandApi.class, isHandlerClass(COMMAND_API)
                .with(method("addRecipe")
                        .thatHandles("example.add-recipe")
                        .withSenderPassThrough()));
    }
}
