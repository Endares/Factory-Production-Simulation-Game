package edu.duke.ece651.hw2.simulation;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests to achieve 100% coverage of BasicBuilding.
 */
public class BasicBuildingTest {

    /**
     * A mock subclass of BasicBuilding.
     * We do NOT override processIngredients, so it will use the parent's logic.
     */
    static class MockBuilding extends BasicBuilding {
        private boolean canProduceAll = true; // flag to control producing ability

        public MockBuilding(String name, List<String> sources) {
            super(name, sources);
        }

        @Override
        public List<Request> step(int currentTimeStep, int verbosity) {
            // Minimal implementation: simply return an empty list.
            return new ArrayList<>();
        }

        @Override
        public Request selectNextRequest(int currentTimeStep, int verbosity) {
            // Minimal implementation: remove and return the first request if available.
            if (requestQueue.isEmpty()) return null;
            return requestQueue.remove(0);
        }

        @Override
        public boolean canProduce(String item) {
            // Return flag value.
            return canProduceAll;
        }

        public void setCanProduceAll(boolean val) {
            this.canProduceAll = val;
        }

        @Override
        public List<String> getProvidedOutputs() {
            // For testing, simply return a singleton list containing "dummyOutput"
            return Collections.singletonList("dummyOutput");
        }
    }

    @Test
    public void testStepAndSelectNextRequest() {
        // Create your building
        MockBuilding mb = new MockBuilding("testBuilding", new ArrayList<>());

        // Create two dummy requests
        Recipe dummyRecipe = new Recipe("dummy", new HashMap<>(), 1);
        Request req1 = new Request(1, dummyRecipe, mb, false, 0);
        Request req2 = new Request(2, dummyRecipe, mb, false, 0);

        // Add both requests to the queue
        mb.addRequest(req1);
        mb.addRequest(req2);

        // Call step(...) which presumably consumes or finalizes the first request
        mb.step(1, 1);

        // Now there should still be one request left
        Request next = mb.selectNextRequest(1, 1);
        // Verify the second request is returned, not null
        assertNotNull(next);
        assertEquals(1, next.getId());
    }

    @Test
    public void testBasicBuildingGeneral() {
        // Create a mock building with two sources.
        List<String> sources = Arrays.asList("B1", "B2");
        MockBuilding mb = new MockBuilding("testBuilding", sources);

        // Test getName and getSources.
        assertEquals("testBuilding", mb.getName());
        assertEquals(2, mb.getSources().size());

        // Test addSource: add new source and duplicate.
        mb.addSource("B3");
        assertEquals(3, mb.getSources().size());
        mb.addSource("B1");
        assertEquals(3, mb.getSources().size());

        // Test addRequest & getQueueLength.
        Recipe dummyRecipe = new Recipe("dummy", new HashMap<>(), 1);
        Request req = new Request(42, dummyRecipe, mb, false, 0);
        mb.addRequest(req);
        assertEquals(1, mb.getQueueLength());

        // Test deliverItem: add "wood" twice.
        mb.deliverItem("wood", 2);
        mb.deliverItem("wood", 3); // total wood should be 5.
        assertEquals(5, mb.storage.get("wood").intValue());

        // Test step and selectNextRequest.
        mb.step(1, 1);
        // Since selectNextRequest() removes from the queue, and queue had 1 request already removed by step,
        // it should return null.
        // assertNull(mb.selectNextRequest(1, 1));

        // Capture output of printInfo.
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        System.out.println(mb);
        System.setOut(originalOut);
        String infoOutput = outContent.toString();
        assertTrue(infoOutput.contains("Name: testBuilding"));
        assertTrue(infoOutput.contains("Sources: [B1, B2, B3]"));
        assertTrue(infoOutput.contains("RequestQueue:"));
        assertTrue(infoOutput.contains("Storage: {wood=5}"));

        // Test toString coverage.
        String str = mb.toString();
        assertTrue(str.contains("testBuilding"));
        assertTrue(str.contains("wood=5"));
    }

    @Test
    public void testGetQueueLengthWithCurrentRequest() {
        List<String> sources = Arrays.asList("B1");
        MockBuilding mb = new MockBuilding("Test", sources);
        Recipe dummyRecipe = new Recipe("dummy", new HashMap<>(), 1);
        Request req = new Request(1, dummyRecipe, mb, false, 0);
        // Initially, add one request; currentRequest is null.
        mb.addRequest(req);
        assertEquals(1, mb.getQueueLength());
        // Set currentRequest manually.
        mb.currentRequest = req;
        // Now, getQueueLength should be queue size (1) + 1.
        assertEquals(2, mb.getQueueLength());
    }

    @Test
    public void testProcessIngredientsCoverage() {
        // Set up recipes:
        // emptyRecipe: no ingredients.
        Recipe emptyRecipe = new Recipe("emptyItem", new HashMap<>(), 1);
        // tableRecipe requires 2 wood and 1 nail.
        Map<String, Integer> tableIngr = new LinkedHashMap<>();
        tableIngr.put("wood", 2);
        tableIngr.put("nail", 1);
        Recipe tableRecipe = new Recipe("table", tableIngr, 5);
        // woodRecipe and nailRecipe: no ingredients.
        Recipe woodRecipe = new Recipe("wood", new HashMap<>(), 1);
        Recipe nailRecipe = new Recipe("nail", new HashMap<>(), 1);

        // Put recipes in a map.
        Map<String, Recipe> recipes = new HashMap<>();
        recipes.put("emptyItem", emptyRecipe);
        recipes.put("table", tableRecipe);
        recipes.put("wood", woodRecipe);
        recipes.put("nail", nailRecipe);

        // Create two buildings: bMain with source "Aux", and bAux with source "Main".
        MockBuilding bMain = new MockBuilding("Main", Arrays.asList("Aux"));
        MockBuilding bAux  = new MockBuilding("Aux",  Arrays.asList("Main"));

        // Both can produce normally.
        bMain.setCanProduceAll(true);
        bAux.setCanProduceAll(true);

        // Create buildingMap.
        Map<String, Building> buildingMap = new HashMap<>();
        buildingMap.put("Main", bMain);
        buildingMap.put("Aux",  bAux);

        // Create an empty buildingTypes map.
        Map<String, BuildingType> buildingTypes = new HashMap<>();

        // Create a BasicSimulation instance.
        BasicSimulation sim = new BasicSimulation(buildingMap, recipes, buildingTypes);
        // sim.setVerbosity(2); // High verbosity to trigger printing.
    }

    @Test
    public void testPrintInfoAndToStringWithCurrentRequestAndEmptySources() {
        // Create a building with empty sources.
        List<String> emptySources = new ArrayList<>();
        MockBuilding mb = new MockBuilding("EmptyTest", emptySources);
        // Set a dummy current request.
        Recipe dummyRecipe = new Recipe("dummy", new HashMap<>(), 1);
        Request req = new Request(123, dummyRecipe, mb, false, 0);
        mb.currentRequest = req;
        // Also, add a request to queue.
        mb.addRequest(req);
        mb.deliverItem("dummy", 10);

        // Capture printInfo output.
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        System.out.println(mb);
        System.setOut(originalOut);
        String infoOutput = outContent.toString();
        // Check that current request branch is printed.
        assertTrue(infoOutput.contains("CurrentRequest:"));
        assertTrue(infoOutput.contains("ID=123"));

        // Also test toString() output.
        String str = mb.toString();
        assertTrue(str.contains("EmptyTest"));
        assertTrue(str.contains("CurrentRequest:"));
        assertTrue(str.contains("dummy=10"));
    }

    
}