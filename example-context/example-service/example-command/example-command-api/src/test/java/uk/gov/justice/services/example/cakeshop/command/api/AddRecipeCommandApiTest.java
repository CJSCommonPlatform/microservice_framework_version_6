package uk.gov.justice.services.example.cakeshop.command.api;


import org.junit.Test;

import static uk.gov.justice.services.test.utils.core.helper.ServiceComponents.verifyPassThroughCommandHandlerMethod;

public class AddRecipeCommandApiTest {

    @Test
    public void shouldHandleAddRecipeCommand() throws Exception {
        verifyPassThroughCommandHandlerMethod(AddRecipeCommandApi.class);
    }
}
