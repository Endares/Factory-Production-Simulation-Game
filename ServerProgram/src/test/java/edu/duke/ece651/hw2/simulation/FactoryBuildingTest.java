package edu.duke.ece651.hw2.simulation;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for FactoryBuilding class.
 */
public class FactoryBuildingTest {

    @Test
    public void testGetProvidedOutputs() {
        BuildingType bt = new BuildingType("fType", Arrays.asList("door", "window"));
        FactoryBuilding factory = new FactoryBuilding("Factory1", bt, Arrays.asList("B1", "B2"));
        assertEquals(Arrays.asList("door", "window"), factory.getProvidedOutputs());
    }

    @Test
    public void testFactoryWithEmptySources() {
        BuildingType bt = new BuildingType("fType", List.of("item1"));
        FactoryBuilding factory = new FactoryBuilding("Factory2", bt, Collections.emptyList());
        // Even if sources are empty, provided outputs are determined solely by building type.
        assertEquals(List.of("item1"), factory.getProvidedOutputs());
    }

    @Test
    public void testProvidedOutputsIndependence() {
        BuildingType bt = new BuildingType("fType", Arrays.asList("a", "b"));
        FactoryBuilding factory = new FactoryBuilding("Factory3", bt, List.of("X"));
        // Ensure a new list is returned each time.
        assertNotSame(factory.getProvidedOutputs(), factory.getProvidedOutputs());
    }

    @Test
    public void testFactoryBuildingSelectNextRequest() {
        // Create recipes
        Recipe metalRecipe = new Recipe("metal", new HashMap<>(), 1);
        
        Map<String, Integer> screwIngr = new HashMap<>();
        screwIngr.put("metal", 1);
        Recipe screwRecipe = new Recipe("screw", screwIngr, 2);
        
        Map<String, Integer> bracketIngr = new HashMap<>();
        bracketIngr.put("metal", 2);
        Recipe bracketRecipe = new Recipe("bracket", bracketIngr, 3);
        
        // Create building types
        BuildingType hardwareType = new BuildingType("Hardware", List.of("screw", "bracket"));
        
        // Create buildings
        MineBuilding metalMine = new MineBuilding("MetalMine", "metal", metalRecipe ,new ArrayList<>());
        
        FactoryBuilding hardware = new FactoryBuilding("Hardware", hardwareType, List.of("MetalMine"));
        
        // Create two requests
        Request screwRequest = new Request(1, screwRecipe, null, true, 0);
        Request bracketRequest = new Request(2, bracketRecipe, null, true, 0);
        
        // Add the requests to the hardware's queue
        hardware.addRequest(screwRequest);
        hardware.addRequest(bracketRequest);
        assertEquals(2, hardware.requestQueue.size());
        
        // Redirect System.out to capture output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        
        try {
            // Test with no ingredients
            int verbosity = 2;
            Request selectedRequest = hardware.selectNextRequest(0, verbosity);
            
            // Verify no request was selected (no ingredients available)
            assertNull(selectedRequest);
            
            // Verify the output shows both requests as not ready
            String output = outContent.toString();
            assertTrue(output.contains("[recipe selection]: Hardware has fifo on cycle 1"));
            assertTrue(output.contains("0: is not ready, waiting on {metal}"));
            assertTrue(output.contains("1: is not ready, waiting on {2x metal}"));
            
            // Reset output
            outContent.reset();
            
            // Add one metal to storage
            hardware.deliverItem("metal", 1);
            
            // Call selectNextRequest again
            selectedRequest = hardware.selectNextRequest(0, verbosity);
            
            // Verify the first request was selected (has enough metal)
            assertNotNull(selectedRequest);
            assertEquals(1, selectedRequest.getId());
            
            // Verify the output shows first request as ready
            output = outContent.toString();

            assertTrue(output.contains("[recipe selection]: Hardware has fifo on cycle 1\n"));
            assertTrue(output.contains("0: is ready\n"));
            assertTrue(output.contains("1: is not ready, waiting on {metal}"));
            assertTrue(output.contains("Selecting 0"));
            
            // Verify hardware's queue now only has the bracket request
            assertEquals(1, hardware.requestQueue.size());
            assertEquals(2, hardware.requestQueue.get(0).getId());
            
            // Reset output
            outContent.reset();
            
            // Add another metal to storage (now 0 after consuming for screw)
            hardware.deliverItem("metal", 2);
            
            // Call selectNextRequest again
            selectedRequest = hardware.selectNextRequest(0, verbosity);
            
            // Verify the second request was selected
            assertNotNull(selectedRequest);
            assertEquals(2, selectedRequest.getId());
            
            // Verify the output shows selection
            output = outContent.toString();
            assertTrue(output.contains("0: is ready"));
            assertTrue(output.contains("Selecting 0"));
            
            // Verify hardware's queue is now empty
            assertEquals(0, hardware.requestQueue.size());
        } finally {
            System.setOut(originalOut);
        }
    }
}