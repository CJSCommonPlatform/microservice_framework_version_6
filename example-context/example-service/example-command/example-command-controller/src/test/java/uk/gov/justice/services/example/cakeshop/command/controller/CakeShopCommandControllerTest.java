package uk.gov.justice.services.example.cakeshop.command.controller;


import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import org.junit.Test;

public class CakeShopCommandControllerTest {

    @Test
    public void shouldBeCommandControllerThatHasSenderPassThroughMethods() throws Exception {
        assertThat(CakeShopCommandController.class, isHandlerClass(COMMAND_CONTROLLER)
                .with(allOf(
                        method("addRecipe")
                                .thatHandles("example.add-recipe")
                                .withSenderPassThrough(),
                        method("renameRecipe")
                                .thatHandles("example.rename-recipe")
                                .withSenderPassThrough(),
                        method("removeRecipe")
                                .thatHandles("example.remove-recipe")
                                .withSenderPassThrough(),
                        method("makeCake")
                                .thatHandles("example.make-cake")
                                .withSenderPassThrough(),
                        method("orderCake")
                                .thatHandles("example.order-cake")
                                .withSenderPassThrough(),
                        method("uploadPhotograph")
                                .thatHandles("example.upload-photograph")
                                .withSenderPassThrough()
                )));
    }
}
