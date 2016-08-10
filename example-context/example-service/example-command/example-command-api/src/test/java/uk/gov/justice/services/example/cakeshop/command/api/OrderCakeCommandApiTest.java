package uk.gov.justice.services.example.cakeshop.command.api;


import org.junit.Test;

import static uk.gov.justice.services.test.utils.core.helper.ServiceComponents.verifyPassThroughCommandHandlerMethod;

public class OrderCakeCommandApiTest {

    @Test
    public void shouldHandleOrderCakeCommand() throws Exception {
        verifyPassThroughCommandHandlerMethod(OrderCakeCommandApi.class);
    }
}
