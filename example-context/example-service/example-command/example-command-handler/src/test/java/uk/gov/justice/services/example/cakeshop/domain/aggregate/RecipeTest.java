package uk.gov.justice.services.example.cakeshop.domain.aggregate;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.example.cakeshop.domain.Ingredient;
import uk.gov.justice.services.example.cakeshop.domain.event.CakeMade;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeRemoved;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeRenamed;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for the {@link Recipe} aggregate class.
 */
public class RecipeTest {

    private static final UUID RECIPE_ID = UUID.randomUUID();
    private static final String NAME = "my recipe";
    private static final Ingredient INGREDIENT = mock(Ingredient.class);
    private static final List<Ingredient> INGREDIENTS = singletonList(INGREDIENT);

    private Recipe recipe;

    @Before
    public void setup() {
        recipe = new Recipe();
    }

    @Test
    public void shouldReturnEventWhenApplied() {
        RecipeAdded event = mock(RecipeAdded.class);
        assertThat(recipe.apply(event), equalTo(event));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionForUnrecognisedEvent() {
        recipe.apply(new Object());
    }

    @Test
    public void shouldReturnRecipeAddedEvent() {

        final Stream<Object> events = recipe.addRecipe(RECIPE_ID, NAME, true, INGREDIENTS);

        final List<Object> eventList = events.collect(toList());
        assertThat(eventList, hasSize(1));

        final Object event = eventList.get(0);
        assertThat(event, instanceOf(RecipeAdded.class));

        final RecipeAdded recipeAdded = (RecipeAdded) event;
        assertThat(recipeAdded.getRecipeId(), equalTo(RECIPE_ID));
        assertThat(recipeAdded.getName(), equalTo(NAME));
        assertThat(recipeAdded.getIngredients(), equalTo(INGREDIENTS));
        assertThat(recipeAdded.isGlutenFree(), is(true));
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotAddRecipeIfAlreadyAdded() {
        try {
            recipe.addRecipe(RECIPE_ID, NAME, false, INGREDIENTS);
        } catch (Exception ex) {
            // Make sure we don't throw an exception the first time.
            fail();
        }
        recipe.addRecipe(RECIPE_ID, NAME, false, INGREDIENTS);
    }

    @Test
    public void shouldReturnRecipeRemovedEvent() {
        recipe.addRecipe(RECIPE_ID, NAME, true, INGREDIENTS);

        final Stream<Object> eventRemoveStream = recipe.removeRecipe();
        final List<Object> eventRemovedList = eventRemoveStream.collect(toList());
        assertThat(eventRemovedList, hasSize(1));

        final Object eventRemove = eventRemovedList.get(0);
        assertThat(eventRemove, instanceOf(RecipeRemoved.class));
    }

    @Test
    public void shouldReturnCakeMadeEvent() throws Exception {
        recipe.addRecipe(RECIPE_ID, NAME, true, INGREDIENTS);

        final UUID cakeId = UUID.randomUUID();

        final Stream<Object> cakeMadeStream = recipe.makeCake(cakeId);
        final List<Object> cakeMadeList = cakeMadeStream.collect(toList());

        assertThat(cakeMadeList, hasSize(1));

        assertThat(cakeMadeList.get(0), instanceOf(CakeMade.class));
        final CakeMade cakeMade = (CakeMade)cakeMadeList.get(0);
        assertThat(cakeMade.getCakeId(), is(cakeId));
        assertThat(cakeMade.getName(), is(NAME));


    }

    @Test
    public void shouldReturnRecipeRenamedEvent() {
        recipe.addRecipe(RECIPE_ID, NAME, true, INGREDIENTS);

        final Stream<Object> eventRenamedStream = recipe.renameRecipe("new name");
        final List<Object> eventRenamedList = eventRenamedStream.collect(toList());
        assertThat(eventRenamedList, hasSize(1));

        final Object eventRenamed = eventRenamedList.get(0);
        assertThat(eventRenamed, instanceOf(RecipeRenamed.class));
    }
}
