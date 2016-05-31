package uk.gov.justice.services.example.cakeshop.query.view.service;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;

import uk.gov.justice.services.example.cakeshop.persistence.RecipeRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipeView;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipesView;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecipeServiceTest {

    public static final String NAME = "name";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID NON_EXISTENT_ID = UUID.randomUUID();
    @InjectMocks
    private RecipeService service;

    @Mock
    private RecipeRepository recipeRepository;

    @Test
    public void shouldReturnRecipeById() {
        Recipe recipe = createRecipe(USER_ID);
        given(recipeRepository.findBy(USER_ID)).willReturn(recipe);

        RecipeView foundPerson = service.findRecipe(USER_ID.toString());

        assertThat(foundPerson.getId(), equalTo(USER_ID));
        assertThat(foundPerson.getName(), equalTo(NAME));
    }

    public void shouldReturnNullWhenRecipeNotFound() {
        given(recipeRepository.findBy(NON_EXISTENT_ID)).willReturn(null);

        assertNull(service.findRecipe(NON_EXISTENT_ID.toString()));
    }

    @Test
    public void shouldReturnRecipeFoundByName() {
        Recipe recipe = createRecipe(USER_ID);
        given(recipeRepository.findByNameIgnoreCase(NAME)).willReturn(singletonList(recipe));

        RecipesView foundRecipes = service.findByName(NAME);

        assertThat(foundRecipes.getRecipes(), hasSize(1));
        assertThat(foundRecipes.getRecipes().get(0).getId(), equalTo(USER_ID));
        assertThat(foundRecipes.getRecipes().get(0).getName(), equalTo(NAME));
    }

    @Test
    public void shouldReturnEmptyListOfNoMatchesOnInvalidName() {
        RecipesView foundRecipes = service.findByName("Invalid Name");

        assertThat(foundRecipes.getRecipes(), hasSize(0));
    }

    @Test
    public void shouldReturnEmptyListOfNoMatchesOnEmptyName() {
        RecipesView foundRecipes = service.findByName("");

        assertThat(foundRecipes.getRecipes(), hasSize(0));
    }

    @Test
    public void shouldGetAlLRecipes() {
        Recipe recipe = createRecipe(USER_ID);
        given(recipeRepository.findAll()).willReturn(singletonList(recipe));
        RecipesView recipes = service.getRecipes();

        List<RecipeView> firstRecipe = recipes.getRecipes();
        assertThat(firstRecipe, hasSize(1));
        assertThat(firstRecipe.get(0).getId(), equalTo(USER_ID));
        assertThat(firstRecipe.get(0).getName(), equalTo(NAME));
    }

    private Recipe createRecipe(final UUID id) {
        return new Recipe(id, NAME);
    }

}