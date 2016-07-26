package uk.gov.justice.services.example.cakeshop.query.api;

import static uk.gov.justice.services.test.utils.helper.ServiceComponents.verifyPassThroughQueryHandlerMethod;

import org.junit.Test;

public class CakeOrdersQueryApiTest {

    @Test
    public void shouldHandleCakeOrderQuery() throws Exception {
        verifyPassThroughQueryHandlerMethod(CakeOrdersQueryApi.class);
    }
}