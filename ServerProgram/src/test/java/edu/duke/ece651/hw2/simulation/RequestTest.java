package edu.duke.ece651.hw2.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Request class.
 */
public class RequestTest {
    /**
     * A minimal dummy building to support testing.
     */
    static class DummyBuilding implements Building {
        private final String name;

        public DummyBuilding(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        // Other methods are not needed for testing Request; provide dummy implementations.
        @Override
        public java.util.List<String> getSources() {
            return null;
        }

        @Override
        public void addRequest(Request request) {
        }

        @Override
        public void addSource(String source) {
        }

        @Override
        public Coordinate getLocation() {
            return null;
        }

        @Override
        public void setLocation(Coordinate location) {
        }

        @Override
        public int getQueueLength() {
            return 0;
        }

        @Override
        public void deliverItem(String item, int quantity) {
        }

        @Override
        public java.util.List<Request> step(int currentTimeStep, int verbosity) {
            return null;
        }

        @Override
        public Request selectNextRequest(int currentTimeStep, int verbosity) {
            return null;
        }

        @Override
        public java.util.List<String> getProvidedOutputs() {
            return null;
        }

        @Override
        public boolean canProduce(String item) {
            return false;
        }

        @Override
        public void processIngredients(String item) {
        }

        @Override
        public void setSimulation(BasicSimulation simulation) {
        }
        
        @Override
        public BasicSimulation getSimulation() {
            return null;
        }

        @Override
        public boolean isViable() { return true; }

        @Override
        public boolean isMarkedForRemoval() { return false; }

        @Override
        public void markForRemoval() {
        }

        @Override
        public boolean canBeRemovedImmediately(){return false;}
    }

    @Test
    public void testRequestConstructorAndGetters() {
        // Create a dummy recipe and building.
        Recipe recipe = new Recipe("testOutput", new HashMap<>(), 5);
        DummyBuilding building = new DummyBuilding("TestBuilding");

        // Create a request with isUserRequest true and timeRequested = 10.
        Request request = new Request(1, recipe, building, true, 10);

        // Verify all getter methods.
        assertEquals(1, request.getId());
        assertEquals(recipe, request.getRecipe());
        assertEquals(building, request.getRequestor());
        assertTrue(request.isUserRequest());
        assertEquals(10, request.getTimeRequested());
        // Default status should be WAITING_FOR_INGREDIENTS.
        assertEquals(RequestStatus.WAITING_FOR_INGREDIENTS, request.getStatus());
    }

    // @Test
    // public void testSetStatusAndCompleted() {
    //     Recipe recipe = new Recipe("output", new HashMap<>(), 3);
    //     DummyBuilding building = new DummyBuilding("B1");
    //     Request request = new Request(2, recipe, building, false, 5);

    //     // Initially, request should not be completed.
    //     assertFalse(request.completed());

    //     // Set status to COMPLETED.
    //     request.setStatus(RequestStatus.COMPLETED);
    //     assertEquals(RequestStatus.COMPLETED, request.getStatus());
    //     assertTrue(request.completed());

    //     // Change status to another value (e.g., IN_PROGRESS) and check completed() returns false.
    //     request.setStatus(RequestStatus.IN_PROGRESS);
    //     assertFalse(request.completed());
    // }

    /**
     * Tests the construction and basic methods of a FactoryBuilding.
     */
    @Test
    public void testFactoryBuildingConstruction() {
        // Create a BuildingType for a door factory that produces "door"
        BuildingType doorFactoryType = new BuildingType("doorFactory", List.of("door"));

        // Define sources for the factory building (names of other buildings)
        List<String> sources = new ArrayList<>();
        sources.add("W");
        sources.add("Ha");
        sources.add("Hi");

        // Construct a FactoryBuilding with name "D"
        FactoryBuilding doorFactory = new FactoryBuilding("D", doorFactoryType, sources);

        // Assert the name is correct
        assertEquals("D", doorFactory.getName(), "Factory building name should be 'D'");

        // Assert the sources are as expected
        List<String> expectedSources = Arrays.asList("W", "Ha", "Hi");
        assertEquals(expectedSources, doorFactory.getSources(), "Factory sources do not match expected");

        // Assert that the provided outputs match the recipes defined in its BuildingType
        List<String> providedOutputs = doorFactory.getProvidedOutputs();
        assertEquals(List.of("door"), providedOutputs, "Factory provided outputs should be ['door']");

        // Assert that the factory can produce "door" but not "wood"
        assertTrue(doorFactory.canProduce("door"), "Factory should be able to produce door");
        assertFalse(doorFactory.canProduce("wood"), "Factory should not be able to produce wood");
    }

    /**
     * Tests the construction and basic methods of a MineBuilding.
     */
    @Test
    public void testMineBuildingConstruction() {
        // Construct a MineBuilding that produces "metal" and has no sources.
        List<String> emptySources = new ArrayList<>();
        // Create a metal recipe (no ingredients, latency 1) and assign it to the mine.
        Recipe metalRecipe = new Recipe("metal", new HashMap<>(), 1);
        MineBuilding metalMine = new MineBuilding("M", "metal", metalRecipe, emptySources);

        // Assert the mine building's name
        assertEquals("M", metalMine.getName(), "Mine building name should be 'M'");

        // Assert that getMine returns the resource this mine produces
        assertEquals("metal", metalMine.getMine(), "Mine building should produce metal");

        // Assert that canProduce returns true for "metal"
        assertTrue(metalMine.canProduce("metal"), "Mine should be able to produce metal");

        // Assert that the sources list is empty
        assertTrue(metalMine.getSources().isEmpty(), "Mine sources should be empty");
    }

    /**
     * Tests that FactoryBuilding and MineBuilding behave correctly when integrated
     * (for example, checking their provided outputs and interactions).
     */
    @Test
    public void testBuildingIntegration() {
        // Create recipes for wood and metal production.
        Recipe woodRecipe = new Recipe("wood", new HashMap<>(), 1);
        Recipe metalRecipe = new Recipe("metal", new HashMap<>(), 1);

        // Create building types for mines.
        BuildingType woodMineType = new BuildingType("woodMine", List.of("wood"));
        BuildingType metalMineType = new BuildingType("metalMine", List.of("metal"));

        // Create a mine for wood.
        MineBuilding woodMine = new MineBuilding("W", "wood", woodRecipe, new ArrayList<>());

        // Create a mine for metal.
        MineBuilding metalMine = new MineBuilding("M", "metal", metalRecipe, new ArrayList<>());

        // Create a factory type for a door factory.
        BuildingType doorFactoryType = new BuildingType("doorFactory", List.of("door"));

        // Assume that door factory requires both wood and metal as ingredients.
        // For simplicity, we just set its sources to include the two mines.
        List<String> doorSources = new ArrayList<>();
        doorSources.add("W");
        doorSources.add("M");
        FactoryBuilding doorFactory = new FactoryBuilding("D", doorFactoryType, doorSources);

        // Verify that the door factory's sources are correct.
        assertEquals(List.of("W", "M"), doorFactory.getSources(), "Door factory sources should be [W, M]");

        // Check provided outputs: doorFactory should provide "door"
        assertEquals(List.of("door"), doorFactory.getProvidedOutputs(), "Door factory provided outputs should be ['door']");
    }
}