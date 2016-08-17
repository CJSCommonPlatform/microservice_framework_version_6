package uk.gov.justice.services.example.cakeshop.command.controller;


import static uk.gov.justice.services.test.utils.core.helper.ServiceComponents.verifyPassThroughCommandHandlerMethod;

import org.junit.Test;

public class CakeShopCommandControllerTest {

    @Test
    public void shouldHandleMakeCakeCommand() throws Exception {
        verifyPassThroughCommandHandlerMethod(CakeShopCommandController.class);
    }
}
