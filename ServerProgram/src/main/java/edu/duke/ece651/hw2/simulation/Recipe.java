package edu.duke.ece651.hw2.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a production recipe.
 */
public class Recipe {
    private String output;
    private Map<String, Integer> ingredients;
    private List<String> orderedIngredientNames; // To preserve the order for DFS
    private int latency;

    /**
     * Constructs a Recipe.
     *
     * @param output      the output product name.
     * @param ingredients map of ingredient names to required amounts.
     * @param latency     production latency (must be between 1 and Integer.MAX_VALUE).
     */
    public Recipe(String output, Map<String, Integer> ingredients, List<String> orderedNames, int latency) {
        this.output = output;
        this.ingredients = new HashMap<>(ingredients);
        this.orderedIngredientNames = new ArrayList<>(orderedNames);
        this.latency = latency;
    }

    // Original constructor (for backwards compatibility with tests)
    public Recipe(String output, Map<String, Integer> ingredients, int latency) {
        this(output, ingredients, new ArrayList<>(ingredients.keySet()), latency);
    }

    /**
     * Gets the output product name.
     *
     * @return output name.
     */
    public String getOutput() {
        return output;
    }

    /**
     * Gets the ingredients map.
     *
     * @return map of ingredients.
     */
    public Map<String, Integer> getIngredients() {
        return new HashMap<>(ingredients);
    }

    /**
     * Gets the production latency.
     *
     * @return latency.
     */
    public int getLatency() {
        return latency;
    }

    /**
     * Checks if this recipe requires a specific ingredient.
     *
     * @param ingredientName the ingredient name to check.
     * @return true if the recipe requires this ingredient, false otherwise.
     */
    public boolean requiresIngredient(String ingredientName) {
        return ingredients.containsKey(ingredientName);
    }

    /**
     * Gets the amount required for a specific ingredient.
     *
     * @param ingredientName the ingredient name.
     * @return the required amount or 0 if not required.
     */
    public int getIngredientAmount(String ingredientName) {
        return ingredients.getOrDefault(ingredientName, 0);
    }

    /**
     * Gets the list of ordered ingredient names.
     *
     * @return the list of ingredient names.
     */
    public List<String> getOrderedIngredientNames() {
        return new ArrayList<>(orderedIngredientNames);
    }

    public String getName() {
        return output;
    }
}