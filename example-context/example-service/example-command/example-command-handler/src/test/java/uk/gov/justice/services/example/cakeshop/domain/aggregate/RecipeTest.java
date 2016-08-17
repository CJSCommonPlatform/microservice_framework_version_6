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
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeRemoved;

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

        Stream<Object> events = recipe.addRecipe(RECIPE_ID, NAME, true, INGREDIENTS);

        List<Object> eventList = events.collect(toList());
        assertThat(eventList, hasSize(1));

        Object event = eventList.get(0);
        assertThat(event, instanceOf(RecipeAdded.class));

        RecipeAdded recipeAdded = (RecipeAdded) event;
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
        Stream<Object> events = recipe.addRecipe(RECIPE_ID, NAME, true, INGREDIENTS);

        List<Object> eventList = events.collect(toList());
        assertThat(eventList, hasSize(1));

        Object event = eventList.get(0);
        assertThat(event, instanceOf(RecipeAdded.class));

        RecipeAdded recipeAdded = (RecipeAdded) event;
        assertThat(recipeAdded.getRecipeId(), equalTo(RECIPE_ID));
        assertThat(recipeAdded.getName(), equalTo(NAME));
        assertThat(recipeAdded.getIngredients(), equalTo(INGREDIENTS));
        assertThat(recipeAdded.isGlutenFree(), is(true));

        Stream<Object> eventRemoveStream = recipe.removeRecipe(RECIPE_ID);
        List<Object> eventRemovedList = eventRemoveStream.collect(toList());
        assertThat(eventRemovedList, hasSize(1));

        Object eventRemove = eventRemovedList.get(0);
        assertThat(eventRemove, instanceOf(RecipeRemoved.class));
    }

}
