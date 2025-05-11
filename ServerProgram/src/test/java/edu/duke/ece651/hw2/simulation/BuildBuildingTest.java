package edu.duke.ece651.hw2.simulation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BuildBuildingTest {
    private BasicSimulation simulation;
    private Map<String, Building> buildings;
    private Map<String, Recipe> recipes;
    private Map<String, BuildingType> buildingTypes;
    private Map<String, BuildableType> buildableTypes;

    @BeforeEach
    public void setUp() {
        // Initialize data structures
        buildings = new HashMap<>();
        recipes = new HashMap<>();
        buildingTypes = new HashMap<>();
        buildableTypes = new HashMap<>();
        
        // Create a simple recipe
        Map<String, Integer> ingredients = new LinkedHashMap<>();
        Recipe boltRecipe = new Recipe("bolt", ingredients, 5);
        recipes.put("bolt", boltRecipe);

        // Create a buildable type for testing
        JSONObject storageInfo = new JSONObject();
        storageInfo.put("stores", "bolt");
        storageInfo.put("capacity", 100);
        storageInfo.put("priority", 1.7);
        
        BuildableType storageType = new BuildableType("Bolt Storage (100)", "storage", storageInfo);
        buildableTypes.put("Bolt Storage (100)", storageType);
        
        // Create an empty factory type for completeness
        JSONObject factoryInfo = new JSONObject();
        BuildingType factoryBuildingType = new BuildingType("Factory", java.util.Arrays.asList("bolt"));
        buildingTypes.put("Factory", factoryBuildingType);
        
        // Create the simulation
        simulation = BasicSimulation.createSimulation(buildings, recipes, buildingTypes, buildableTypes);
    }

    @Test
    public void testBuildStorageBuilding() throws SimulationException {
        // Test coordinates
        int x = 10, y = 20;
        
        // Execute the build command
        BuildCommand buildCmd = new BuildCommand(x, y, "Bolt Storage (100)");
        buildCmd.execute(simulation);
        
        // Verify a building was added to the simulation
        Map<String, Building> updatedBuildings = simulation.getBuildings();
        assertTrue(updatedBuildings.size() > 0, "Building should be added to the simulation");
        
        // Find the newly created building (it should have a name like "Bolt_Storage__100__1")
        Building newBuilding = null;
        String newBuildingName = null;
        for (Map.Entry<String, Building> entry : updatedBuildings.entrySet()) {
            if (entry.getKey().startsWith("Bolt_Storage")) {
                newBuilding = entry.getValue();
                newBuildingName = entry.getKey();
                break;
            }
        }
        
        assertNotNull(newBuilding, "New building should exist");
        
        // Verify it's the right type of building
        assertTrue(newBuilding instanceof StorageBuilding, "Building should be a StorageBuilding");
        StorageBuilding storageBuilding = (StorageBuilding) newBuilding;
        
        // Verify the building properties
        assertEquals("bolt", storageBuilding.getStoredItem(), "Building should store bolts");
        assertEquals(100, storageBuilding.getCapacity(), "Building should have capacity of 100");
        
        // Verify the building location
        Coordinate expectedLocation = new Coordinate(x, y);
        assertEquals(expectedLocation, storageBuilding.getLocation(), "Building should be at the specified location");
        
        // Verify the building is registered in the road map
        RoadMap roadMap = simulation.getRoadMap();
        Map<Coordinate, Building> buildingLocations = roadMap.getBuildingLocations();
        assertSame(newBuilding, buildingLocations.get(expectedLocation), "Building should be registered in the road map");
    }

    @Test
    public void testBuildBuildingInvalidLocation() {
        // First, build a building at location (10, 20)
        try {
            simulation.buildBuilding("Bolt Storage (100)", 10, 20);
        } catch (SimulationException e) {
            fail("Should not throw exception for first building");
        }
        
        // Now try to build a second building at the same location
        SimulationException exception = assertThrows(
            SimulationException.class,
            () -> simulation.buildBuilding("Bolt Storage (100)", 10, 20),
            "Building at an occupied location should throw SimulationException"
        );
        
        assertTrue(exception.getMessage().contains("already occupied"), 
                  "Exception message should mention the location is already occupied");
    }

    @Test
    public void testBuildBuildingInvalidType() {
        SimulationException exception = assertThrows(
            SimulationException.class,
            () -> simulation.buildBuilding("Invalid Type", 10, 20),
            "Building with invalid type should throw SimulationException"
        );
        
        assertTrue(exception.getMessage().contains("does not exist"), 
                  "Exception message should mention the type does not exist");
    }
}