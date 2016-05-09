package uk.gov.justice.services.example.cakeshop.persistence;

import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecipeRepositoryTest {

    private static final String RECIPE_NAME = "Chocolate Cake";
    private static final UUID RECIPE_ID = UUID.randomUUID();

    @Mock
    private EntityManager entityManager;

    @Mock
    private Recipe recipe;

    @Mock
    private Recipe recipeB;

    @Mock
    private Query query;

    @InjectMocks
    private RecipeRepository recipeRepository;

    @Test
    public void shouldSave() throws Exception {
        recipeRepository.save(recipe);

        verify(entityManager).persist(recipe);
    }

    @Test
    public void shouldFindById() throws Exception {
        when(entityManager.find(Recipe.class, RECIPE_ID)).thenReturn(recipe);

        Optional<Recipe> actualRecipe = recipeRepository.findById(RECIPE_ID);

        assertThat(actualRecipe.get(), sameInstance(recipe));
    }

    @Test
    public void shouldReturnOptionalEmpty() throws Exception {
        when(entityManager.find(Recipe.class, RECIPE_ID)).thenReturn(null);

        Optional<Recipe> actualRecipe = recipeRepository.findById(RECIPE_ID);

        assertThat(actualRecipe, equalTo(empty()));
    }

    @Test
    public void shouldReturnListOfRecipes() {
        when(entityManager.createNamedQuery("Recipe.findByNameIgnoreCase")).thenReturn(query);
        when(query.setParameter("name", "%" + RECIPE_NAME.toLowerCase() + "%")).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(recipe));


        List<Recipe> recipeList = recipeRepository.findByNameIgnoreCase(RECIPE_NAME);

        assertThat(recipeList, IsCollectionWithSize.hasSize(1));
        assertThat(recipeList, IsCollectionContaining.hasItems(recipe));
    }

    @Test
    public void shouldReturnEmptyListOfRecipesIfSearchDoesNotMatch() {
        when(entityManager.createNamedQuery("Recipe.findByNameIgnoreCase")).thenReturn(query);
        when(query.setParameter("name", "%" + RECIPE_NAME.toLowerCase() + "%")).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());


        List<Recipe> recipeList = recipeRepository.findByNameIgnoreCase(RECIPE_NAME);

        assertThat(recipeList, IsCollectionWithSize.hasSize(0));
    }

    @Test
    public void shouldReturnEmptyListOfOnSearchRecipesIfNoRecipesExist() {
        when(entityManager.createNamedQuery("Recipe.findByNameIgnoreCase")).thenReturn(query);
        when(query.setParameter("name", "%" + RECIPE_NAME.toLowerCase() + "%")).thenReturn(query);
        when(query.getResultList()).thenReturn(null);


        List<Recipe> recipeList = recipeRepository.findByNameIgnoreCase(RECIPE_NAME);

        assertThat(recipeList, IsCollectionWithSize.hasSize(0));
    }

    @Test
    public void shouldReturnAllRecipes() {
        when(entityManager.createNamedQuery("Recipe.getRecipes")).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(recipe, recipeB));

        List<Recipe> recipeList = recipeRepository.getAllRecipes();

        assertThat(recipeList, IsCollectionWithSize.hasSize(2));
        assertThat(recipeList, IsCollectionContaining.hasItems(recipe));
        assertThat(recipeList, IsCollectionContaining.hasItems(recipeB));
    }

    @Test
    public void shouldReturnEmptyListOfRecipesIfNoRecipesExist() {
        when(entityManager.createNamedQuery("Recipe.getRecipes")).thenReturn(query);
        when(query.getResultList()).thenReturn(null);

        List<Recipe> recipeList = recipeRepository.getAllRecipes();

        assertThat(recipeList, IsCollectionWithSize.hasSize(0));
    }
}