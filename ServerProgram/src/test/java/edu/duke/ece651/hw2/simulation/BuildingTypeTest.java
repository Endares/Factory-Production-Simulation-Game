package edu.duke.ece651.hw2.simulation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for BuildingType class.
 */
public class BuildingTypeTest {

    @Test
    public void testBuildingTypeGetters() {
        List<String> recipes = Arrays.asList("door", "handle");
        BuildingType type = new BuildingType("factory", recipes);
        assertEquals("factory", type.getName());
        assertEquals(recipes, type.getRecipes());
    }

    @Test
    public void testBuildingTypeWithEmptyRecipes() {
        BuildingType type = new BuildingType("empty", Collections.emptyList());
        assertEquals("empty", type.getName());
        assertTrue(type.getRecipes().isEmpty());
    }

    @Test
    public void testGetName() {
        // Create a BuildingType instance with a name and recipes list.
        String typeName = "FactoryType";
        List<String> recipes = Arrays.asList("recipe1", "recipe2");
        BuildingType bt = new BuildingType(typeName, recipes);

        // Verify that getName() returns the expected name.
        assertEquals(typeName, bt.getName());
    }

    @Test
    public void testGetRecipes() {
        // Create a BuildingType instance with a recipes list.
        List<String> recipes = Arrays.asList("recipeA", "recipeB", "recipeC");
        BuildingType bt = new BuildingType("TestType", recipes);

        // Verify that getRecipes() returns the exact recipes list.
        assertEquals(recipes, bt.getRecipes());
    }
}