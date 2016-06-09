package uk.gov.justice.services.example.cakeshop.persistence;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import uk.gov.justice.services.example.cakeshop.persistence.entity.Ingredient;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class IngredientRepositoryTest {

    private static final UUID INGREDIENT = UUID.randomUUID();
    private static final String INGREDIENT_NAME_A = "Flour";
    private static final UUID INGREDIENT_ID_B = UUID.randomUUID();
    private static final String INGREDIENT_NAME_B = "Egg";
    private static final UUID INGREDIENT_ID_C = UUID.randomUUID();
    private static final String INGREDIENT_NAME_C = "Chocolate";

    @Inject
    private IngredientRepository ingredientRepository;

    private Ingredient ingredientA;
    private Ingredient ingredientB;
    private Ingredient ingredientC;

    @Before
    public void setup() {
        ingredientA = createIngredient(INGREDIENT, INGREDIENT_NAME_A);
        ingredientRepository.save(ingredientA);
        ingredientB = createIngredient(INGREDIENT_ID_B, INGREDIENT_NAME_B);
        ingredientRepository.save(ingredientB);
        ingredientC = createIngredient(INGREDIENT_ID_C, INGREDIENT_NAME_C);
        ingredientRepository.save(ingredientC);
    }

    @Test
    public void shouldFindIngredientById() {
        Ingredient ingredient = ingredientRepository.findBy(INGREDIENT);

        assertThat(ingredient, is(notNullValue()));
        assertThat(ingredient.getId(), equalTo(INGREDIENT));
        assertThat(ingredient.getName(), equalTo(INGREDIENT_NAME_A));
    }

    @Test
    public void shouldReturnNullIfIngredientNotFound() {
        Ingredient ingredient = ingredientRepository.findBy(UUID.randomUUID());

        assertThat(ingredient, is(nullValue()));
    }

    @Test
    public void shouldReturnIngredientsMatchingName() {
        List<Ingredient> ingredientList = ingredientRepository.findByNameIgnoreCase(INGREDIENT_NAME_A);

        assertThat(ingredientList, hasSize(1));
        assertThat(ingredientList.get(0).getId(), equalTo(INGREDIENT));
        assertThat(ingredientList.get(0).getName(), equalTo(INGREDIENT_NAME_A));
    }

    @Test
    public void shouldReturnListOfIngredientsMatchingName() {
        List<Ingredient> ingredientList = ingredientRepository.findByNameIgnoreCase("%o%");

        assertThat(ingredientList, hasSize(2));
        assertThat(ingredientList, hasItems(ingredientA));
        assertThat(ingredientList, hasItems(ingredientC));
    }

    @Test
    public void shouldReturnEmptyListOfIngredientsIfSearchDoesNotMatch() {
        List<Ingredient> ingredientList = ingredientRepository.findByNameIgnoreCase("InvalidName");

        assertThat(ingredientList, hasSize(0));
    }

    @Test
    public void shouldReturnAllIngredients() {
        List<Ingredient> ingredientList = ingredientRepository.findAll();

        assertThat(ingredientList, hasSize(3));
        assertThat(ingredientList, hasItems(ingredientA));
        assertThat(ingredientList, hasItems(ingredientB));
        assertThat(ingredientList, hasItems(ingredientC));
    }

    private Ingredient createIngredient(UUID id, String name) {
        return new Ingredient(id, name);
    }
}