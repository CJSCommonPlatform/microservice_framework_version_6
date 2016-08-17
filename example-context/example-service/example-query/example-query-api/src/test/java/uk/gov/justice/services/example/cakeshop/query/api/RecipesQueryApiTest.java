package uk.gov.justice.services.example.cakeshop.query.api;


import static uk.gov.justice.services.test.utils.core.helper.ServiceComponents.verifyPassThroughQueryHandlerMethod;

import org.junit.Test;

public class RecipesQueryApiTest {

    @Test
    public void shouldHandleRecipesQuery() throws Exception {
        verifyPassThroughQueryHandlerMethod(RecipesQueryApi.class);
    }
}
