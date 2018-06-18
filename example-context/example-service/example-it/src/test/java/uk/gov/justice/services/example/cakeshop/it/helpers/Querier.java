package uk.gov.justice.services.example.cakeshop.it.helpers;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.QUERY_CAKES_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.QUERY_ORDER_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.QUERY_RECIPES_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.QUERY_RECIPE_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.CAKES_RESOURCE_QUERY_URI;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.ORDERS_RESOURCE_QUERY_URI;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.RECIPES_RESOURCE_QUERY_URI;

import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

public class Querier {

    private final Client client;

    public Querier(final Client client) {
        this.client = client;
    }

    public ApiResponse queryForRecipe(final String recipeId) {
        final Response jaxrsResponse = client.target(RECIPES_RESOURCE_QUERY_URI + recipeId).request().accept(QUERY_RECIPE_MEDIA_TYPE).get();
        return ApiResponse.from(jaxrsResponse);
    }

    public ApiResponse queryForOrder(final String orderId) {
        final Response jaxrsResponse = client.target(ORDERS_RESOURCE_QUERY_URI + orderId).request().accept(QUERY_ORDER_MEDIA_TYPE).get();
        return ApiResponse.from(jaxrsResponse);
    }

    public ApiResponse recipesQueryResult() {
        return recipesQueryResult(singletonList(new BasicNameValuePair("pagesize", "50")));
    }

    public ApiResponse recipesQueryResult(final List<NameValuePair> queryParams) {
        try {
            final URIBuilder uri = new URIBuilder(RECIPES_RESOURCE_QUERY_URI);
            uri.addParameters(queryParams);
            final Response jaxrRsResponse = client.target(uri.toString()).request().accept(QUERY_RECIPES_MEDIA_TYPE).get();
            return ApiResponse.from(jaxrRsResponse);
        } catch (URISyntaxException e) {
            fail(e.getMessage());
            return null;
        }
    }

    public ApiResponse cakesQueryResult() {
        final Response jaxrsResponse = client.target(CAKES_RESOURCE_QUERY_URI).request().accept(QUERY_CAKES_MEDIA_TYPE).get();
        assertThat(jaxrsResponse.getStatus(), is(OK.getStatusCode()));
        return ApiResponse.from(jaxrsResponse);

    }
}
