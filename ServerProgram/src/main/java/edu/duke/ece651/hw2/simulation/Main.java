package edu.duke.ece651.hw2.simulation;

import java.io.IOException;

import org.json.JSONException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Main class for running the simulation.
 *
 * <p>
 * Usage: java simulation.Main &lt;json-file&gt;
 * <p>
 * The JSON file is expected to conform to the specifications for recipes, types, and buildings.
 * After parsing and validating the JSON input, the simulation enters an interactive command loop.
 * </p>
 */
public class Main {
    /**
     * Entry point for the simulation.
     *
     * @param args command line arguments; the first argument should be the JSON file path.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java simulation.Main <json-file>");
            System.exit(0);
        }
        String filePath = args[0];
        SimulationParser parser = new SimulationParser();

        try {
            JsonNode json = parser.parseJsonFile(filePath);
            // Parse recipes
            java.util.Map<String, Recipe> recipes = parser.parseRecipes(json);
            // Parse types
            java.util.Map<String, BuildingType> buildingTypes = parser.parseTypes(json, recipes);
            // Parse buildings
            java.util.Map<String, Building> buildings = parser.parseBuildings(json, buildingTypes, recipes);
            // Validate the complete input
            parser.validateInput(buildings, recipes);
            // Create simulation and run interactive loop
            BasicSimulation simulation = BasicSimulation.createSimulation(buildings, recipes, buildingTypes);
            // Add connections from the JSON
            parser.parseConnections(json, simulation);
            simulation.runInteractive();
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (JSONException e) {
            System.err.println("Invalid JSON format: " + e.getMessage());
        } catch (SimulationException e) {
            System.err.println("Simulation error: " + e.getMessage());
        }
    }
}