package uk.gov.justice.services.example.cakeshop.query.api;


import org.junit.Test;

import static uk.gov.justice.services.test.utils.core.helper.ServiceComponents.verifyPassThroughQueryHandlerMethod;

public class RecipesQueryApiTest {

    @Test
    public void shouldHandleRecipesQuery() throws Exception {
        verifyPassThroughQueryHandlerMethod(RecipesQueryApi.class);
    }
}