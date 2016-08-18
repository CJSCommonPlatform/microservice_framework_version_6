package uk.gov.justice.services.example.cakeshop.query.controller;


import static uk.gov.justice.services.test.utils.core.helper.ServiceComponents.verifyPassThroughQueryHandlerMethod;

import org.junit.Test;

public class RecipesQueryControllerTest {

    @Test
    public void shouldHandleRecipesQuery() throws Exception {
        verifyPassThroughQueryHandlerMethod(RecipesQueryController.class);
    }
}
