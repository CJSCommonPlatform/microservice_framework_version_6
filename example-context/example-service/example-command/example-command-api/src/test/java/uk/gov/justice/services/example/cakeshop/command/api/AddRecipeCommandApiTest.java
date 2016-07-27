package uk.gov.justice.services.example.cakeshop.command.api;

import static uk.gov.justice.services.test.utils.helper.ServiceComponents.verifyPassThroughCommandHandlerMethod;

import org.junit.Test;

public class AddRecipeCommandApiTest {

    @Test
    public void shouldHandleAddRecipeCommand() throws Exception {
        verifyPassThroughCommandHandlerMethod(AddRecipeCommandApi.class);
    }
}
