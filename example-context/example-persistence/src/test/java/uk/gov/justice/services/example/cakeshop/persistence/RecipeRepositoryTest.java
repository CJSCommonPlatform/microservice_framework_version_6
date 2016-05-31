package uk.gov.justice.services.example.cakeshop.persistence;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class RecipeRepositoryTest {

    private static final UUID RECIPE_ID_A = UUID.randomUUID();
    private static final String RECIPE_NAME_A = "Chocolate Cake";
    private static final UUID RECIPE_ID_B = UUID.randomUUID();
    private static final String RECIPE_NAME_B = "Sponge Cake";
    private static final UUID RECIPE_ID_C = UUID.randomUUID();
    private static final String RECIPE_NAME_C = "Muffin";

    @Inject
    private RecipeRepository recipeRepository;

    private Recipe recipeA;
    private Recipe recipeB;
    private Recipe recipeC;

    @Before
    public void setup() {
        recipeA = createRecipe(RECIPE_ID_A, RECIPE_NAME_A);
        recipeRepository.save(recipeA);
        recipeB = createRecipe(RECIPE_ID_B, RECIPE_NAME_B);
        recipeRepository.save(recipeB);
        recipeC = createRecipe(RECIPE_ID_C, RECIPE_NAME_C);
        recipeRepository.save(recipeC);
    }

    @Test
    public void shouldFindRecipeById() {
        Recipe recipe = recipeRepository.findBy(RECIPE_ID_A);

        assertThat(recipe, is(notNullValue()));
        assertThat(recipe.getId(), equalTo(RECIPE_ID_A));
        assertThat(recipe.getName(), equalTo(RECIPE_NAME_A));
    }

    @Test
    public void shouldReturnNullIfRecipeNotFound() {
        Recipe recipe = recipeRepository.findBy(UUID.randomUUID());

        assertThat(recipe, is(nullValue()));
    }

    @Test
    public void shouldReturnRecipesMatchingName() {
        List<Recipe> recipeList = recipeRepository.findByNameIgnoreCase(RECIPE_NAME_A);

        assertThat(recipeList, hasSize(1));
        assertThat(recipeList.get(0).getId(), equalTo(RECIPE_ID_A));
        assertThat(recipeList.get(0).getName(), equalTo(RECIPE_NAME_A));
    }

    @Test
    public void shouldReturnListOfRecipesMatchingName() {
        List<Recipe> recipeList = recipeRepository.findByNameIgnoreCase("%Cake%");

        assertThat(recipeList, hasSize(2));
        assertThat(recipeList, hasItems(recipeA));
        assertThat(recipeList, hasItems(recipeB));
    }

    @Test
    public void shouldReturnEmptyListOfRecipesIfSearchDoesNotMatch() {
        List<Recipe> recipeList = recipeRepository.findByNameIgnoreCase("InvalidName");

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

    private Recipe createRecipe(UUID id, String name) {
        return new Recipe(id, name);
    }
}