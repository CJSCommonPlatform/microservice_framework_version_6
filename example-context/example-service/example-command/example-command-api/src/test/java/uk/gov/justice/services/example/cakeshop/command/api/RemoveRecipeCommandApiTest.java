package uk.gov.justice.services.example.cakeshop.command.api;

import static uk.gov.justice.services.test.utils.core.helper.ServiceComponents.verifyPassThroughCommandHandlerMethod;

import org.junit.Test;


public class RemoveRecipeCommandApiTest {

    @Test
    public void shouldHandleRemoveRecipeCommand() throws Exception {
        verifyPassThroughCommandHandlerMethod(RemoveRecipeCommandApi.class);
    }
}
