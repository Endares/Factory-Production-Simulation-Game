package edu.duke.ece651.hw2.simulation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for Recipe class.
 */
public class RecipeTest {

    @Test
    public void testRecipeGettersUsingBackwardsConstructor() {
        // Using the backwards-compatible constructor.
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("wood", 2);
        ingredients.put("nail", 4);
        Recipe recipe = new Recipe("door", ingredients, 10);
        
        // Test getOutput
        assertEquals("door", recipe.getOutput());
        // Test getIngredients returns a copy equal to the original ingredients map
        assertEquals(ingredients, recipe.getIngredients());
        // Test getLatency
        assertEquals(10, recipe.getLatency());
        
        // Test getOrderedIngredientNames: should contain same keys as ingredients.
        List<String> ordered = recipe.getOrderedIngredientNames();
        assertNotNull(ordered);
        // Since HashMap keyset order is not guaranteed, check size and contents.
        assertEquals(ingredients.size(), ordered.size());
        for (String key : ingredients.keySet()) {
            assertTrue(ordered.contains(key));
        }
    }
    
    @Test
    public void testRecipeGettersUsingFullConstructor() {
        // Using the constructor that accepts ordered names explicitly.
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("metal", 3);
        ingredients.put("screw", 10);
        List<String> orderedNames = Arrays.asList("screw", "metal");
        Recipe recipe = new Recipe("table", ingredients, orderedNames, 5);
        
        // Test getOutput
        assertEquals("table", recipe.getOutput());
        // Test getIngredients
        assertEquals(ingredients, recipe.getIngredients());
        // Test getLatency
        assertEquals(5, recipe.getLatency());
        // Test getOrderedIngredientNames returns a copy with same order as provided.
        List<String> returnedOrder = recipe.getOrderedIngredientNames();
        assertEquals(orderedNames, returnedOrder);
        
        // Modify the returned list and ensure original ordering in recipe is not affected.
        returnedOrder.add("extra");
        assertNotEquals(returnedOrder, recipe.getOrderedIngredientNames());
    }
    
    @Test
    public void testRequiresIngredient() {
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("plastic", 5);
        Recipe recipe = new Recipe("bottle", ingredients, 2);
        
        // Test requiresIngredient for existing and non-existing keys.
        assertTrue(recipe.requiresIngredient("plastic"));
        assertFalse(recipe.requiresIngredient("glass"));
    }
    
    @Test
    public void testGetIngredientAmount() {
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("sugar", 3);
        Recipe recipe = new Recipe("candy", ingredients, 4);
        
        // Check amount for present ingredient
        assertEquals(3, recipe.getIngredientAmount("sugar"));
        // Check for an ingredient not in the recipe
        assertEquals(0, recipe.getIngredientAmount("salt"));
    }
    
    @Test
    public void testGetOrderedIngredientNamesIndependence() {
        // Create a recipe with an explicit ordered list.
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("flour", 2);
        ingredients.put("egg", 1);
        List<String> order = new ArrayList<>(Arrays.asList("egg", "flour"));
        Recipe recipe = new Recipe("cake", ingredients, order, 3);
        
        // Get the ordered ingredient names and modify the list.
        List<String> retrieved = recipe.getOrderedIngredientNames();
        assertEquals(order, retrieved);
        retrieved.add("milk");
        
        // The internal state should remain unchanged.
        List<String> afterModification = recipe.getOrderedIngredientNames();
        assertEquals(order, afterModification);
    }
}