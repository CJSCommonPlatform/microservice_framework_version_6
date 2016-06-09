package uk.gov.justice.services.example.cakeshop.event.listener.converter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Ingredient;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecipeAddedToIngredientsConverterTest {

    private static final String NAME = "ingredientName";

    @Mock
    private RecipeAdded recipeAdded;

    private RecipeAddedToIngredientsConverter converter;

    @Before
    public void setup() {
        when(recipeAdded.getIngredients()).thenReturn(Collections.singletonList(new uk.gov.justice.services.example.cakeshop.domain.Ingredient(NAME, 2)));
        converter = new RecipeAddedToIngredientsConverter();
    }

    @Test
    public void shouldConvertRecipeAddedEvent() {
        List<Ingredient> ingredients = converter.convert(recipeAdded);

        assertThat(ingredients.size(), equalTo(1));
        assertThat(ingredients.get(0).getName(), equalTo(NAME));
    }
}