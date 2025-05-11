package edu.duke.ece651.hw2.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;

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
public class NewMain {
    /**
     * 从给定的 BuildableType 集合中提取所有 factory 类型，
     * 并转换成旧版的 BuildingType map。
     *
     * @param buildableTypes 已解析的 BuildableType 映射
     * @return 仅包含 factory 类型的 BuildingType 映射
     */
    public static Map<String, BuildingType> extractBuildingTypes(Map<String, BuildableType> buildableTypes) {
        Map<String, BuildingType> buildingTypes = new HashMap<>();
        for (BuildableType bt : buildableTypes.values()) {
            List<String> typeRecipes = new ArrayList<>();
            if ("factory".equals(bt.getType())) {
                JSONObject info = bt.getInfo();
                org.json.JSONArray arr = info.getJSONArray("recipes");
                for (int i = 0; i < arr.length(); i++) {
                    typeRecipes.add(arr.getString(i));
                }
            }
            // 非 factory 类型保持 typeRecipes 为空
            buildingTypes.put(bt.getName(), new BuildingType(bt.getName(), typeRecipes));
        }
        return buildingTypes;
    }

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
            Map<String, Recipe> recipes = parser.parseRecipes(json);
            // Parse types
            Map<String, BuildableType> buildableTypes = parser.parseBuildableTypes(json, recipes);
            Map<String, BuildingType> buildingTypes = extractBuildingTypes(buildableTypes);
            // Parse buildings
            Map<String, Building> buildings = parser.parseBuildings(json, buildableTypes, buildingTypes, recipes);
            // Validate the complete input
            parser.validateInput(buildings, recipes);
            // Create simulation and run interactive loop
            BasicSimulation simulation = BasicSimulation.createSimulation(buildings, recipes, buildingTypes, buildableTypes);
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