package uk.gov.justice.services.example.cakeshop.event.listener.converter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.example.cakeshop.domain.Ingredient;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;

import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecipeAddedToRecipeConverterTest {

    private static final UUID ID = UUID.randomUUID();

    private static final String NAME = "recipeName";

    @Mock
    private RecipeAdded recipeAdded;

    private RecipeAddedToRecipeConverter converter;

    @Before
    public void setup() {
        when(recipeAdded.getName()).thenReturn(NAME);
        when(recipeAdded.getIngredients()).thenReturn(Collections.singletonList(new Ingredient("sugar", 2)));
        converter = new RecipeAddedToRecipeConverter();
    }

    @Test
    public void shouldConvertRecipeAddedEvent() {
        Recipe recipe = converter.convert(recipeAdded);

        assertThat(recipe.getName(), equalTo(NAME));
    }


}