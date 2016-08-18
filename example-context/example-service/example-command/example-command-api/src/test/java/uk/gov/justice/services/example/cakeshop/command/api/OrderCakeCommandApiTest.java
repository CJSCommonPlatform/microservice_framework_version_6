package uk.gov.justice.services.example.cakeshop.command.api;


import static uk.gov.justice.services.test.utils.core.helper.ServiceComponents.verifyPassThroughCommandHandlerMethod;

import org.junit.Test;

public class OrderCakeCommandApiTest {

    @Test
    public void shouldHandleOrderCakeCommand() throws Exception {
        verifyPassThroughCommandHandlerMethod(OrderCakeCommandApi.class);
    }
}
