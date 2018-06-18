package uk.gov.justice.services.example.cakeshop.it;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.UUID.fromString;
import static javax.ws.rs.client.Entity.entity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.ADD_RECIPE_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.CONTEXT_NAME;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.RECIPES_RESOURCE_URI;

import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.example.cakeshop.it.helpers.CakeShopRepositoryManager;
import uk.gov.justice.services.example.cakeshop.it.helpers.CommandFactory;
import uk.gov.justice.services.example.cakeshop.it.helpers.RestEasyClientFactory;

import java.util.Optional;

import javax.ws.rs.client.Client;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CakeShopEventBufferingIT {

    private static final CakeShopRepositoryManager CAKE_SHOP_REPOSITORY_MANAGER = new CakeShopRepositoryManager();

    private final CommandFactory commandFactory = new CommandFactory();

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
    public void shouldUpdateEventBufferStatus() throws Exception {
        final String recipeId = "163af847-effb-46a9-96bc-32a0f7526f89";

        client.target(RECIPES_RESOURCE_URI + recipeId)
                .request()
                .post(entity(commandFactory.addRecipeCommand(), ADD_RECIPE_MEDIA_TYPE));

        await().until(() -> streamStatus(recipeId).isPresent());
        assertThat(streamStatus(recipeId).get().getVersion(), is(1L));
    }

    @SuppressWarnings("SameParameterValue")
    private Optional<StreamStatus> streamStatus(final String recipeId) {
        return CAKE_SHOP_REPOSITORY_MANAGER.getStreamStatusJdbcRepository().findByStreamIdAndSource(fromString(recipeId), CONTEXT_NAME);
    }
}
