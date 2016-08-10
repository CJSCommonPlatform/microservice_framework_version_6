package uk.gov.justice.services.example.cakeshop.query.controller;


import org.junit.Test;

import static uk.gov.justice.services.test.utils.core.helper.ServiceComponents.verifyPassThroughQueryHandlerMethod;

public class RecipesQueryControllerTest {

    @Test
    public void shouldHandleRecipesQuery() throws Exception {
        verifyPassThroughQueryHandlerMethod(RecipesQueryController.class);
    }
}