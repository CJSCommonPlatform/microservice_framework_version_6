package uk.gov.justice.services.example.cakeshop.command.controller;


import org.junit.Test;

import static uk.gov.justice.services.test.utils.core.helper.ServiceComponents.verifyPassThroughCommandHandlerMethod;

public class CakeShopCommandControllerTest {

    @Test
    public void shouldHandleMakeCakeCommand() throws Exception {
        verifyPassThroughCommandHandlerMethod(CakeShopCommandController.class);
    }
}