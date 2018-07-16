package uk.gov.justice.services.example.cakeshop.it.helpers;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.MAKE_CAKE_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.MAKE_CAKE_STATUS_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.CAKES_RESOURCE_URI_FORMAT;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.RECIPES_RESOURCE_URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

public class CommandSender {

    private final Client client;
    private final EventFactory eventFactory;

    public CommandSender(final Client client, final EventFactory eventFactory) {
        this.client = client;
        this.eventFactory = eventFactory;
    }

    public ApiResponse makeCake(final String recipeId, final String cakeId) {
        final Response jaxrRsResponse = client.target(format(CAKES_RESOURCE_URI_FORMAT, recipeId, cakeId))
                .request()
                .accept(MAKE_CAKE_STATUS_MEDIA_TYPE)
                .post(entity("{}", MAKE_CAKE_MEDIA_TYPE));
        assertThat(jaxrRsResponse.getStatus(), is(ACCEPTED.getStatusCode()));

        return ApiResponse.from(jaxrRsResponse);
    }

    public void addRecipe(final String recipeId, final String cakeName) {
        client.target(RECIPES_RESOURCE_URI + recipeId).request()
                .post(eventFactory.recipeEntity(cakeName, false));
    }
}
