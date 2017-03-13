package uk.gov.justice.services.example.cakeshop.command.api;


import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import org.junit.Test;

public class RecipeCommandApiTest {

    @Test
    public void shouldHandleRecipeCommands() throws Exception {
        assertThat(RecipeCommandApi.class, isHandlerClass(COMMAND_API)
                .with(method("addRecipe")
                        .thatHandles("example.add-recipe")
                        .withSenderPassThrough())
                .with(method("renameRecipe")
                        .thatHandles("example.rename-recipe")
                        .withSenderPassThrough())
                .with(method("removeRecipe")
                        .thatHandles("example.remove-recipe")
                        .withSenderPassThrough())
                .with(method("uploadPhotograph")
                        .thatHandles("example.upload-photograph")
                        .withSenderPassThrough())
        );
    }
}
