package uk.gov.justice.services.example.cakeshop.it;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import uk.gov.justice.domain.snapshot.AggregateSnapshot;
import uk.gov.justice.domain.snapshot.DefaultObjectInputStreamStrategy;
import uk.gov.justice.services.core.aggregate.exception.AggregateChangeDetectedException;
import uk.gov.justice.services.eventsourcing.jdbc.snapshot.SnapshotJdbcRepository;
import uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe;
import uk.gov.justice.services.example.cakeshop.it.helpers.CakeShopRepositoryManager;
import uk.gov.justice.services.example.cakeshop.it.helpers.CommandSender;
import uk.gov.justice.services.example.cakeshop.it.helpers.EventFactory;
import uk.gov.justice.services.example.cakeshop.it.helpers.Querier;
import uk.gov.justice.services.example.cakeshop.it.helpers.RestEasyClientFactory;

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.Client;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SnapshotCakeShopIT {

    private static final CakeShopRepositoryManager CAKE_SHOP_REPOSITORY_MANAGER = new CakeShopRepositoryManager();

    private final EventFactory eventFactory = new EventFactory();

    private Client client;
    private Querier querier;
    private CommandSender commandSender;

    @BeforeClass
    public static void beforeClass() throws Exception {
        CAKE_SHOP_REPOSITORY_MANAGER.initialise();
    }

    @Before
    public void before() throws Exception {
        client = new RestEasyClientFactory().createResteasyClient();
        querier = new Querier(client);
        commandSender = new CommandSender(client, eventFactory);
    }

    @After
    public void cleanup() throws Exception {
        client.close();
    }

    @Test
    public void shouldUseSnapshotWhenMakingCakes() throws AggregateChangeDetectedException {

        final String recipeId = randomUUID().toString();
        final String cakeName = "Delicious cake";

        commandSender.addRecipe(recipeId, cakeName);
        await().until(() -> querier.recipesQueryResult().body().contains(recipeId));

        //cake made events belong to the recipe aggregate.
        //snapshot threshold is set to 3 in settings-test.xml so this should cause snapshot to be created
        final String cakeId1 = randomUUID().toString();
        final String cakeId2 = randomUUID().toString();

        commandSender.makeCake(recipeId, cakeId1);
        commandSender.makeCake(recipeId, cakeId2);

        await().until(() -> recipeAggregateSnapshotOf(recipeId).isPresent());

        final String newCakeName = "Tweaked cake";
        changeRecipeSnapshotName(recipeId, newCakeName);

        final String lastCakeId = randomUUID().toString();
        commandSender.makeCake(recipeId, lastCakeId);

        await().until(() -> querier.cakesQueryResult().body().contains(lastCakeId));

        with(querier.cakesQueryResult().body())
                .assertThat("$.cakes[?(@.id == '" + lastCakeId + "')].name", hasItem(newCakeName));

    }

    @SuppressWarnings("unchecked")
    private void changeRecipeSnapshotName(final String recipeId, final String newRecipeName) throws AggregateChangeDetectedException {
        final AggregateSnapshot<Recipe> recipeAggregateSnapshot = recipeAggregateSnapshotOf(recipeId).get();
        final Recipe recipe = recipeAggregateSnapshot.getAggregate(new DefaultObjectInputStreamStrategy());
        setField(recipe, "name", newRecipeName);
        final SnapshotJdbcRepository snapshotJdbcRepository = CAKE_SHOP_REPOSITORY_MANAGER.getSnapshotJdbcRepository();
        snapshotJdbcRepository.removeAllSnapshots(recipeAggregateSnapshot.getStreamId(), Recipe.class);
        snapshotJdbcRepository.storeSnapshot(new AggregateSnapshot(recipeAggregateSnapshot.getStreamId(), recipeAggregateSnapshot.getVersionId(), recipe));
    }

    private Optional<AggregateSnapshot<Recipe>> recipeAggregateSnapshotOf(final String recipeId) {
        final SnapshotJdbcRepository snapshotJdbcRepository = CAKE_SHOP_REPOSITORY_MANAGER.getSnapshotJdbcRepository();
        return snapshotJdbcRepository.getLatestSnapshot(UUID.fromString(recipeId), Recipe.class);
    }
}
