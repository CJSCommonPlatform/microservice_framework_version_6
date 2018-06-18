package uk.gov.justice.services.example.cakeshop.it;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsNull.nullValue;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.ORDERS_RESOURCE_URI;

import uk.gov.justice.services.example.cakeshop.it.helpers.CakeShopRepositoryManager;
import uk.gov.justice.services.example.cakeshop.it.helpers.RestEasyClientFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CakeShopCrossOriginResourceSharingIT {

    private static final CakeShopRepositoryManager CAKE_SHOP_REPOSITORY_MANAGER = new CakeShopRepositoryManager();

    private Client client;

    @BeforeClass
    public static void beforeClass() throws Exception {
        CAKE_SHOP_REPOSITORY_MANAGER.initialise();
    }

    @Before
    public void before() throws Exception {
        client = new RestEasyClientFactory().createResteasyClient();
    }

    @After
    public void cleanup() throws Exception {
        client.close();
    }

    @Test
    public void shouldReturnCORSResponse() {
        final Response corsResponse =
                client.target(ORDERS_RESOURCE_URI + "123")
                        .request()
                        .header("Origin", "http://foo.example")
                        .header("Access-Control-Request-Headers", "CPPCLIENTCORRELATIONID")
                        .options();

        assertThat(corsResponse.getStatus(), is(OK.getStatusCode()));
        final String allowedHeaders = corsResponse.getHeaderString("access-control-allow-headers");
        assertThat(allowedHeaders, not(nullValue()));
        assertThat(asList(allowedHeaders.split(", ")), hasItems("CJSCPPUID", "CPPSID", "CPPCLIENTCORRELATIONID"));
    }
}
