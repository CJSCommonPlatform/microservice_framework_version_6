package uk.gov.justice.services.example.cakeshop.persistence;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;
import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class RecipeRepositoryIT extends BaseTransactionalTest {

    private static final UUID RECIPE_ID_A = UUID.randomUUID();
    private static final String RECIPE_NAME_A = "Chocolate Cake";
    private static final UUID RECIPE_ID_B = UUID.randomUUID();
    private static final String RECIPE_NAME_B = "Sponge Cake";
    private static final UUID RECIPE_ID_C = UUID.randomUUID();
    private static final String RECIPE_NAME_C = "Muffin";
    private static final boolean RECIPE_GLUTEN_FREE_A = true;

    @Inject
    private RecipeRepository recipeRepository;

    private Recipe recipeA;
    private Recipe recipeB;
    private Recipe recipeC;

    @Override
    protected void setUpBefore() {
        recipeC = createRecipe(RECIPE_ID_C, RECIPE_NAME_C, true);
        recipeRepository.save(recipeC);
        recipeA = createRecipe(RECIPE_ID_A, RECIPE_NAME_A, RECIPE_GLUTEN_FREE_A);
        recipeRepository.save(recipeA);
        recipeB = createRecipe(RECIPE_ID_B, RECIPE_NAME_B, false);
        recipeRepository.save(recipeB);
    }

    @Test
    public void shouldFindRecipeById() {
        Recipe recipe = recipeRepository.findBy(RECIPE_ID_A);

        assertThat(recipe, is(notNullValue()));
        assertThat(recipe.getId(), equalTo(RECIPE_ID_A));
        assertThat(recipe.getName(), equalTo(RECIPE_NAME_A));
        assertThat(recipe.isGlutenFree(), is(RECIPE_GLUTEN_FREE_A));
    }

    @Test
    public void shouldReturnPage() throws Exception {
        final int pageSize = 2;
        List<Recipe> recipeList = recipeRepository.findBy(pageSize, Optional.empty(), Optional.empty());

        assertThat(recipeList, hasSize(2));
        assertThat(recipeList, hasItems(recipeA, recipeC));

    }

    @Test
    public void shouldReturnNullIfRecipeNotFound() {
        Recipe recipe = recipeRepository.findBy(UUID.randomUUID());

        assertThat(recipe, is(nullValue()));
    }


    @Test
    public void shouldReturnListOfRecipesMatchingName() {
        List<Recipe> recipeList = recipeRepository.findBy(10, Optional.of("Cake"), Optional.empty());

        assertThat(recipeList, hasSize(2));
        assertThat(recipeList, hasItems(recipeA, recipeB));
    }

    @Test
    public void shouldReturnListOfGlutenFreeOfRecipes() {
        List<Recipe> recipeList = recipeRepository.findBy(10, Optional.empty(), Optional.of(true));

        assertThat(recipeList, hasSize(2));
        assertThat(recipeList, hasItems(recipeA, recipeC));
    }


    @Test
    public void shouldReturnEmptyListOfRecipesIfSearchDoesNotMatch() {
        List<Recipe> recipeList = recipeRepository.findBy(10, Optional.of("InvalidName"), Optional.empty());

        assertThat(recipeList, hasSize(0));
    }

    @Test
    public void shouldReturnAllRecipes() {
        List<Recipe> recipeList = recipeRepository.findAll();

        assertThat(recipeList, hasSize(3));
        assertThat(recipeList, hasItems(recipeA));
        assertThat(recipeList, hasItems(recipeB));
        assertThat(recipeList, hasItems(recipeC));
    }

    @Test
    public void shouldRemoveARecipeFromARecipes() {
        List<Recipe> recipeList = recipeRepository.findAll();

        assertThat(recipeList, hasSize(3));
        assertThat(recipeList, hasItems(recipeA));
        assertThat(recipeList, hasItems(recipeB));
        assertThat(recipeList, hasItems(recipeC));

        removeAndAssertRecipe(recipeA.getId());
        removeAndAssertRecipe(recipeB.getId());
        removeAndAssertRecipe(recipeC.getId());

        List<Recipe> remainingRecipes = recipeRepository.findAll();

        assertThat(remainingRecipes, hasSize(0));
    }

    private void removeAndAssertRecipe(UUID recipeId) {
        Recipe foundRecipe = recipeRepository.findBy(recipeId);
        recipeRepository.remove(foundRecipe);
        Recipe recipeFound = recipeRepository.findBy(recipeId);

        Assert.assertNull(recipeFound);
    }

    private Recipe createRecipe(final UUID id, final String name, final boolean glutenFree) {
        return new Recipe(id, name, glutenFree, null);
    }
}
