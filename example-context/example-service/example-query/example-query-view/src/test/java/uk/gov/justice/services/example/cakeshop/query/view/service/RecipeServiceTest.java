package uk.gov.justice.services.example.cakeshop.query.view.service;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;

import uk.gov.justice.services.example.cakeshop.persistence.RecipeRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipeView;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipesView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecipeServiceTest {

    private static final String NAME = "name";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID NON_EXISTENT_ID = UUID.randomUUID();
    private static final boolean GLUTEN_FREE = true;
    private static final UUID PHOTO_ID = UUID.randomUUID();
    @InjectMocks
    private RecipeService service;

    @Mock
    private RecipeRepository recipeRepository;

    @Test
    public void shouldReturnRecipeById() {
        given(recipeRepository.findBy(USER_ID)).willReturn(new Recipe(USER_ID, NAME, GLUTEN_FREE, PHOTO_ID));

        RecipeView foundPerson = service.findRecipe(USER_ID.toString());

        assertThat(foundPerson.getId(), equalTo(USER_ID));
        assertThat(foundPerson.getName(), equalTo(NAME));
    }

    public void shouldReturnNullWhenRecipeNotFound() {
        given(recipeRepository.findBy(NON_EXISTENT_ID)).willReturn(null);

        assertNull(service.findRecipe(NON_EXISTENT_ID.toString()));
    }

    @Test
    public void shouldGetRecipes() {
        int pageSize = 20;
        Optional<String> nameQueryParam = Optional.of("name123");
        Optional<Boolean> glutenFreeQueryParam = Optional.of(false);
        given(recipeRepository.findBy(pageSize, nameQueryParam, glutenFreeQueryParam))
                .willReturn(singletonList(new Recipe(USER_ID, NAME, GLUTEN_FREE, PHOTO_ID)));
        RecipesView recipes = service.getRecipes(pageSize, nameQueryParam, glutenFreeQueryParam);

        List<RecipeView> firstRecipe = recipes.getRecipes();
        assertThat(firstRecipe, hasSize(1));
        assertThat(firstRecipe.get(0).getId(), equalTo(USER_ID));
        assertThat(firstRecipe.get(0).getName(), equalTo(NAME));
        assertThat(firstRecipe.get(0).isGlutenFree(), is(GLUTEN_FREE));
    }

    @Test
    public void shouldGetRecipes2() {
        int pageSize = 10;

        Optional<String> nameQueryParam = Optional.of("other name");
        Optional<Boolean> glutenFreeQueryParam = Optional.empty();

        given(recipeRepository.findBy(pageSize, nameQueryParam, glutenFreeQueryParam))
                .willReturn(singletonList(new Recipe(USER_ID, NAME, GLUTEN_FREE, PHOTO_ID)));

        RecipesView recipes = service.getRecipes(pageSize, nameQueryParam, glutenFreeQueryParam);

        List<RecipeView> firstRecipe = recipes.getRecipes();
        assertThat(firstRecipe, hasSize(1));
        assertThat(firstRecipe.get(0).getId(), equalTo(USER_ID));
        assertThat(firstRecipe.get(0).getName(), equalTo(NAME));
        assertThat(firstRecipe.get(0).isGlutenFree(), is(GLUTEN_FREE));
    }

}