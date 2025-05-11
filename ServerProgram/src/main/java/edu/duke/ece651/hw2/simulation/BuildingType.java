package edu.duke.ece651.hw2.simulation;

import java.util.List;

/**
 * Represents a building type.
 */
public class BuildingType {
    private String name;
    private List<String> recipes;

    /**
     * Constructs a BuildingType.
     *
     * @param name    the building type name.
     * @param recipes list of recipe names associated with this type.
     */
    public BuildingType(String name, List<String> recipes) {
        this.name = name;
        this.recipes = recipes;
    }

    /**
     * Gets the building type name.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the list of recipe names.
     *
     * @return list of recipes.
     */
    public List<String> getRecipes() {
        return recipes;
    }
}