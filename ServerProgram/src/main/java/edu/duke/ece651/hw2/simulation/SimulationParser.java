package edu.duke.ece651.hw2.simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

/**
 * Parses JSON configuration files for the simulation.
 */
public class SimulationParser {
    private ObjectMapper mapper;

    public SimulationParser() {
        this.mapper = new ObjectMapper();
    }

    /**
     * Reads and parses the JSON file from the given file path.
     *
     * @param filePath the path to the JSON file.
     * @return JsonNode representing the parsed JSON.
     * @throws IOException   if file reading fails.
     */
    public JsonNode parseJsonFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return mapper.readTree(content);
    }

    /**
     * Parses the recipes from the given JSON object.
     * <p>
     * Each recipe must contain:
     * <ul>
     *   <li>output (String without the character ')</li>
     *   <li>ingredients (JSONObject mapping ingredient names to amounts; each ingredient must be defined as a recipe)</li>
     *   <li>latency (an integer between 1 and Integer.MAX_VALUE)</li>
     * </ul>
     *
     * @param json the JSON object.
     * @return map of recipe output names to Recipe objects.
     * @throws SimulationException if required fields are missing or invalid.
     */
    public Map<String, Recipe> parseRecipes(JsonNode json) throws SimulationException {
        Map<String, Recipe> recipes = new HashMap<>();
        if (!json.has("recipes")) {
            throw new SimulationException("Missing 'recipes' field");
        }
        JsonNode recipesArray = json.get("recipes");
        if (!recipesArray.isArray()) {
            throw new SimulationException("'recipes' field is not an array");
        }
        for (JsonNode recObj : recipesArray) {
            if (!recObj.has("output") || !recObj.has("ingredients") || !recObj.has("latency")) {
                throw new SimulationException("Recipe missing required field(s)");
            }
            String output = recObj.get("output").asText();
            if (output.contains("'")) {
                throw new SimulationException("Recipe output contains invalid character: " + output);
            }
            JsonNode ingredientsNode = recObj.get("ingredients");
            if (!ingredientsNode.isObject()) {
                throw new SimulationException("Invalid ingredients format for recipe: " + output);
            }
            // 使用 LinkedHashMap 保证插入顺序
            Map<String, Integer> ingredients = new LinkedHashMap<>();
            List<String> orderedIngredientNames = new ArrayList<>();
            Iterator<String> fieldNames = ingredientsNode.fieldNames();
            while (fieldNames.hasNext()) {
                String ingredient = fieldNames.next();
                if (ingredient.contains("'")) {
                    throw new SimulationException("Ingredient name contains invalid character: " + ingredient);
                }
                int amount = ingredientsNode.get(ingredient).asInt();
                ingredients.put(ingredient, amount);
                orderedIngredientNames.add(ingredient);
            }
            int latency = recObj.get("latency").asInt();
            if (latency < 1) {
                throw new SimulationException("Latency must be >= 1");
            }
            if (recipes.containsKey(output)) {
                throw new SimulationException("Duplicate recipe name: " + output);
            }
            Recipe recipe = new Recipe(output, ingredients, orderedIngredientNames, latency);
            recipes.put(output, recipe);
        }
        return recipes;
    }

    /**
     * Parses the new "types" section into BuildableType objects,
     * 并对 factory 类型校验 info.recipes 中的每个名称都已在 recipes 中定义。
     *
     * @param json      整个 JSON 文件对应的 JsonNode
     * @param recipes   已解析的 Recipe 映射，用于校验 factory 中的 recipe 名称
     * @return BuildableType 映射
     * @throws SimulationException 发现格式错误或 factory 引用了未定义的 recipe 时抛出
     */
    public Map<String, BuildableType> parseBuildableTypes(JsonNode json,
                                                          Map<String, Recipe> recipes)
            throws SimulationException {
        if (!json.has("types")) {
            throw new SimulationException("Missing 'types' field");
        }
        JsonNode typesArray = json.get("types");
        if (!typesArray.isArray()) {
            throw new SimulationException("'types' field is not an array");
        }
        Map<String, BuildableType> result = new LinkedHashMap<>();
        for (JsonNode tObj : typesArray) {
            if (!tObj.has("name") || !tObj.has("type") || !tObj.has("info")) {
                throw new SimulationException("Type entry missing 'name', 'type' or 'info'");
            }
            String name = tObj.get("name").asText();
            String kind = tObj.get("type").asText();
            JsonNode infoNode = tObj.get("info");
            if (!infoNode.isObject()) {
                throw new SimulationException("'info' for type " + name + " must be an object");
            }
            JSONObject infoJson = new JSONObject(infoNode.toString());

            // 如果是 factory 类型，校验 info.recipes 列表内的每个名称都已在 recipes 中
            if ("factory".equals(kind)) {
                if (!infoJson.has("recipes")) {
                    throw new SimulationException("Factory type '" + name + "' missing 'recipes' array");
                }
                org.json.JSONArray arr = infoJson.getJSONArray("recipes");
                for (int i = 0; i < arr.length(); i++) {
                    String recipeName = arr.getString(i);
                    if (!recipes.containsKey(recipeName)) {
                        throw new SimulationException("Factory type '" + name +
                                "' references undefined recipe: " + recipeName);
                    }
                }
            }

            result.put(name, new BuildableType(name, kind, infoJson));
        }
        return result;
    }

    /**
     * Parses building types from the given JSON object.
     * <p>
     * Each type must contain:
     * <ul>
     *   <li>name (String without the character ' and must be unique)</li>
     *   <li>recipes (an array of recipe names already defined in recipes)</li>
     * </ul>
     *
     * @param json    the JSON object.
     * @param recipes map of recipes for validation.
     * @return map of building type names to BuildingType objects.
     * @throws SimulationException if required fields are missing or invalid.
     */
    public Map<String, BuildingType> parseTypes(JsonNode json, Map<String, Recipe> recipes) throws SimulationException {
        Map<String, BuildingType> buildingTypes = new HashMap<>();
        if (!json.has("types")) {
            throw new SimulationException("Missing 'types' field");
        }
        JsonNode typesArray = json.get("types");
        if (!typesArray.isArray()) {
            throw new SimulationException("'types' field is not an array");
        }
        for (JsonNode typeObj : typesArray) {
            if (!typeObj.has("name") || !typeObj.has("recipes")) {
                throw new SimulationException("Type missing required field(s)");
            }
            String name = typeObj.get("name").asText();
            if (name.contains("'")) {
                throw new SimulationException("Type name contains invalid character: " + name);
            }
            if (buildingTypes.containsKey(name)) {
                throw new SimulationException("Duplicate type name: " + name);
            }
            JsonNode recipesArray = typeObj.get("recipes");
            if (!recipesArray.isArray()) {
                throw new SimulationException("Type recipes field is not an array for type: " + name);
            }
            List<String> typeRecipes = new ArrayList<>();
            for (JsonNode recipeNode : recipesArray) {
                String recipeName = recipeNode.asText();
                if (!recipes.containsKey(recipeName)) {
                    throw new SimulationException("Type references undefined recipe: " + recipeName);
                }
                typeRecipes.add(recipeName);
            }
            BuildingType bt = new BuildingType(name, typeRecipes);
            buildingTypes.put(name, bt);
        }
        return buildingTypes;
    }

    /**
     * Parses buildings from the given JSON object.
     *
     * @param json          the JSON object.
     * @param buildingTypes map of building types for validation.
     * @param recipes       map of recipes for validation.
     * @return map of building names to Building objects.
     * @throws SimulationException if required fields are missing or invalid.
     */
    public Map<String, Building> parseBuildings(JsonNode json, Map<String, BuildingType> buildingTypes, Map<String, Recipe> recipes) throws SimulationException {
        Map<String, Building> buildings = new HashMap<>();
        if (!json.has("buildings")) {
            throw new SimulationException("Missing 'buildings' field");
        }
        JsonNode buildingsArray = json.get("buildings");
        if (!buildingsArray.isArray()) {
            throw new SimulationException("'buildings' field is not an array");
        }
        for (JsonNode bObj : buildingsArray) {
            if (!bObj.has("name")) {
                throw new SimulationException("Building missing 'name' field");
            }
            String name = bObj.get("name").asText();
            if (name.contains("'")) {
                throw new SimulationException("Building name contains invalid character: " + name);
            }
            if (buildings.containsKey(name)) {
                throw new SimulationException("Duplicate building name: " + name);
            }
            // 解析公共的 sources 字段
            List<String> sources = new ArrayList<>();
            if (bObj.has("sources")) {
                JsonNode srcArray = bObj.get("sources");
                if (srcArray.isArray()) {
                    for (JsonNode src : srcArray) {
                        sources.add(src.asText());
                    }
                }
            }

            // 检查 StorageBuilding：必须包含 "stores"
            if (bObj.has("stores")) {
                // 同时不允许含有 "type" 或 "mine"
                if (bObj.has("type") || bObj.has("mine")) {
                    throw new SimulationException("Building " + name + " cannot have both 'stores' and 'type/mine' fields");
                }
                String storedItem = bObj.get("stores").asText();
                if (!bObj.has("capacity") || !bObj.has("priority")) {
                    throw new SimulationException("Storage building " + name + " must have 'capacity' and 'priority' fields");
                }
                int capacity = bObj.get("capacity").asInt();
                float priority = (float) bObj.get("priority").asDouble();
                StorageBuilding storageBuilding = new StorageBuilding(name, storedItem, capacity, priority, sources);
                buildings.put(name, storageBuilding);
            } else if (bObj.has("type")) {
                // Factory building
                if (bObj.has("mine")) {
                    throw new SimulationException("Building " + name + " cannot have both 'type' and 'mine'");
                }
                String type = bObj.get("type").asText();
                if (!buildingTypes.isEmpty() && !buildingTypes.containsKey(type)) {
                    throw new SimulationException("Undefined building type for building: " + name);
                }
                FactoryBuilding factory = new FactoryBuilding(name, buildingTypes.get(type), sources);
                buildings.put(name, factory);
            } else if (bObj.has("mine")) {
                // Mine building
                String mine = bObj.get("mine").asText();
                if (!recipes.containsKey(mine)) {
                    throw new SimulationException("Undefined mine recipe for building: " + name);
                }
                MineBuilding mineBuilding = new MineBuilding(name, mine, recipes.get(mine), sources);
                buildings.put(name, mineBuilding);
            } else if (!bObj.has("type") && !bObj.has("mine") && !bObj.has("stores")
                    && bObj.has("x") && bObj.has("y")) {
                // 构造 DroneBuilding
                DroneBuilding droneBuilding = new DroneBuilding(name);
                int x = bObj.get("x").asInt();
                int y = bObj.get("y").asInt();
                droneBuilding.setLocation(new Coordinate(x, y));
                buildings.put(name, droneBuilding);
            } else {
                throw new SimulationException("Building " + name + " must have either 'type', 'mine', or 'stores' field, or be a Drone Port Building");
            }
            // 读取 x、y 坐标（如果有）
            if (bObj.has("x") && bObj.has("y")) {
                int x = bObj.get("x").asInt();
                int y = bObj.get("y").asInt();
                buildings.get(name).setLocation(new Coordinate(x, y));
            }
        }
        assignBuildingLocations(buildings);
        return buildings;
    }

    /**
     * Parses the "buildings" section, using BuildableType.info to drive each subtype.
     *
     * @param json            整个 JSON
     * @param buildableTypes  从 parseBuildableTypes 得到的 BuildableType map
     * @param buildingTypes extractFactoryTypes(...) 得到的 factory->BuildingType map
     * @param recipes         解析过的 Recipe map
     */
    public Map<String, Building> parseBuildings(
            JsonNode json,
            Map<String, BuildableType> buildableTypes,
            Map<String, BuildingType> buildingTypes,
            Map<String, Recipe> recipes
    ) throws SimulationException {
        if (!json.has("buildings")) {
            throw new SimulationException("Missing 'buildings' field");
        }
        Map<String, Building> buildings = new HashMap<>();
        for (JsonNode bObj : json.get("buildings")) {
            // name
            if (!bObj.has("name")) {
                throw new SimulationException("Building missing 'name' field");
            }
            String name = bObj.get("name").asText();
            if (buildings.containsKey(name)) {
                throw new SimulationException("Duplicate building name: " + name);
            }

            // typeName
            if (!bObj.has("type")) {
                throw new SimulationException("Building " + name + " missing 'type' field");
            }
            String typeName = bObj.get("type").asText();
            BuildableType bt = buildableTypes.get(typeName);
            if (bt == null) {
                throw new SimulationException("Unknown building type: " + typeName);
            }

            // sources
            List<String> sources = new ArrayList<>();
            if (bObj.has("sources")) {
                for (JsonNode s : bObj.get("sources")) {
                    sources.add(s.asText());
                }
            }

            // instantiate based on bt.getType()
            Building bld;
            String kind = bt.getType();
            JSONObject info = bt.getInfo();
            switch (kind) {
                case "factory" -> {
                    BuildingType oldBT = buildingTypes.get(typeName);
                    if (oldBT == null) {
                        throw new SimulationException("No factory definition for type: " + typeName);
                    }
                    bld = new FactoryBuilding(name, oldBT, sources);
                }
                case "storage" -> {
                    String stores = info.getString("stores");
                    int capacity = info.getInt("capacity");
                    double prio = info.getDouble("priority");
                    bld = new StorageBuilding(name, stores, capacity, (float)prio, sources);
                }
                case "mine" -> {
                    String mineRecipeName = info.getString("mine");
                    Recipe r = recipes.get(mineRecipeName);
                    if (r == null) {
                        throw new SimulationException("Undefined mine recipe: " + mineRecipeName);
                    }
                    bld = new MineBuilding(name, mineRecipeName, r, sources);
                }
                case "drone" -> bld = new DroneBuilding(name);
                default -> throw new SimulationException("Unsupported kind: " + kind);
            }

            // coordinates
            if (bObj.has("x") && bObj.has("y")) {
                int x = bObj.get("x").asInt();
                int y = bObj.get("y").asInt();
                bld.setLocation(new Coordinate(x, y));
            }

            buildings.put(name, bld);
        }

        assignBuildingLocations(buildings);
        return buildings;
    }


    private void assignBuildingLocations(Map<String, Building> buildings) throws SimulationException {
        List<Building> unassigned = new ArrayList<>();
        List<Building> assigned = new ArrayList<>();
        // 将已有坐标的建筑加入 assigned 列表
        for (Building b : buildings.values()) {
            if (b.getLocation() != null) {
                assigned.add(b);
            } else {
                unassigned.add(b);
            }
        }
        // 如果没有任何建筑有坐标，则第一个建筑放在 (0,0)
        if (assigned.isEmpty() && !buildings.isEmpty()) {
            Building first = buildings.values().iterator().next();
            first.setLocation(new Coordinate(0, 0));
            assigned.add(first);
            unassigned.remove(first);
        }

        // 为剩余未分配坐标的建筑逐个寻找合适的候选坐标
        for (Building b : unassigned) {
            boolean found = false;
            // 采用遍历候选区域
            for (int x = 0; x <= 100 && !found; x++) {
                for (int y = 0; y <= 100 && !found; y++) {
                    Coordinate candidate = new Coordinate(x, y);
                    // 条件一：候选位置与所有已分配建筑在任一维度上距离至少为 5
                    boolean okSpacing = true;
                    for (Building a : assigned) {
                        Coordinate loc = a.getLocation();
                        if (Math.abs(candidate.getX() - loc.getX()) < 5 || Math.abs(candidate.getY() - loc.getY()) < 5) {
                            okSpacing = false;
                            break;
                        }
                    }
                    if (!okSpacing) continue;

                    // 条件二：新建筑自身必须至少在 x 和 y 两个方向上分别有一个邻居（距离 ≤ 10）
                    boolean hasNeighborX = false;
                    boolean hasNeighborY = false;
                    for (Building a : assigned) {
                        Coordinate loc = a.getLocation();
                        if (Math.abs(candidate.getX() - loc.getX()) <= 10)
                            hasNeighborX = true;
                        if (Math.abs(candidate.getY() - loc.getY()) <= 10)
                            hasNeighborY = true;
                    }
                    if (!hasNeighborX || !hasNeighborY) continue;

                    // 条件三：对于已分配的建筑，如果它们在 x 或 y 方向上尚未有邻居（距离 ≤ 10），
                    // 则候选位置必须为它们提供邻居。注意：对于初次放置可能不需要额外补偿，
                    // 但对于后续建筑，已分配建筑应尽量得到邻居。
                    boolean helpsAll = true;
                    for (Building a : assigned) {
                        Coordinate loc = a.getLocation();
                        // 检查 a 是否已有 x 方向邻居
                        boolean aHasNeighborX = false;
                        boolean aHasNeighborY = false;
                        for (Building other : assigned) {
                            if (other == a) continue;
                            Coordinate oloc = other.getLocation();
                            if (Math.abs(loc.getX() - oloc.getX()) <= 10) aHasNeighborX = true;
                            if (Math.abs(loc.getY() - oloc.getY()) <= 10) aHasNeighborY = true;
                        }
                        // 如果 a 在某一方向没有邻居，则 candidate 必须与 a在该方向距离 ≤ 10
                        if (!aHasNeighborX && Math.abs(candidate.getX() - loc.getX()) > 10) {
                            helpsAll = false;
                            break;
                        }
                        if (!aHasNeighborY && Math.abs(candidate.getY() - loc.getY()) > 10) {
                            helpsAll = false;
                            break;
                        }
                    }
                    if (!helpsAll) continue;

                    // 找到满足所有条件的候选位置
                    b.setLocation(candidate);
                    assigned.add(b);
                    found = true;
                }
            }
            if (!found) {
                throw new SimulationException("无法为建筑 " + b.getName() + " 分配合适的坐标");
            }
        }
    }

    /**
     * 解析 JSON 文件中定义的连接操作。
     * <p>
     * 设计的 JSON 格式如下：
     * <pre>
     * "connections": [
     *     { "source": "BuildingA", "destination": "BuildingB" },
     *     { "source": "BuildingC", "destination": "BuildingD" }
     * ]
     * </pre>
     * 对于每个连接，调用 simulation.connectBuildings(source, destination) 建立两建筑间的连接。
     *
     * @param json       整个 JSON 文件对应的 JsonNode 对象。
     * @param simulation 已创建的 BasicSimulation 对象。
     * @throws SimulationException 如果连接数据格式不正确或其他错误发生。
     */
    public void parseConnections(JsonNode json, BasicSimulation simulation) throws SimulationException {
        if (json.has("connections")) {
            JsonNode connectionsArray = json.get("connections");
            if (!connectionsArray.isArray()) {
                throw new SimulationException("'connections' field is not an array");
            }
            for (JsonNode connNode : connectionsArray) {
                if (!connNode.has("source") || !connNode.has("destination")) {
                    throw new SimulationException("Connection entry missing 'source' or 'destination' field");
                }
                String source = connNode.get("source").asText();
                String destination = connNode.get("destination").asText();
                simulation.connectBuildings(source, destination);
            }
        }
    }

    /**
     * Validates the simulation input data.
     * <p>
     * This includes:
     * <ul>
     *   <li>Checking that ingredient keys in recipes exist as defined recipes.</li>
     *   <li>Ensuring that each building's sources refer to defined buildings.</li>
     *   <li>For factory buildings:
     *       <ul>
     *         <li>Each recipe (as per its BuildingType) must have at least one ingredient.</li>
     *         <li>Every ingredient required must be provided by at least one source building.
     *             (A mine provides its single 'mine' output; a factory provides the outputs of its type.)</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * @param buildings     map of buildings.
     * @param recipes       map of recipes.
     * @throws SimulationException if validation fails.
     */
    public void validateInput(Map<String, Building> buildings, Map<String, Recipe> recipes) throws SimulationException {
        // Validate that ingredients in recipes exist in recipes.
        for (Recipe recipe : recipes.values()) {
            for (String ingredient : recipe.getIngredients().keySet()) {
                if (!recipes.containsKey(ingredient)) {
                    throw new SimulationException("Recipe " + recipe.getOutput() + " has undefined ingredient: " + ingredient);
                }
            }
        }

        // Validate factory buildings.
        for (Building building : buildings.values()) {
            if (building instanceof FactoryBuilding factory) {
                BuildingType bt = factory.getBuildingType();
                // Each recipe in the factory type must have at least one ingredient.
                for (String recipeName : bt.getRecipes()) {
                    Recipe recipe = recipes.get(recipeName);
                    if (recipe.getIngredients().isEmpty()) {
                        throw new SimulationException("Factory building " + building.getName() +
                                " has recipe " + recipeName + " with no ingredients");
                    }
                    // For each ingredient, check that at least one source building provides it.
                    for (String ingredient : recipe.getIngredients().keySet()) {
                        boolean provided = false;
                        for (String srcName : building.getSources()) {
                            if (!buildings.containsKey(srcName)) {
                                throw new SimulationException("Building " + building.getName() + " has undefined source: " + srcName);
                            }
                            Building srcBuilding = buildings.get(srcName);
                            if (srcBuilding.getProvidedOutputs().contains(ingredient)) {
                                provided = true;
                                break;
                            }
                        }
                        if (!provided) {
                            throw new SimulationException("No source for ingredient " + ingredient +
                                    " required by recipe " + recipeName + " for factory " + building.getName());
                        }
                    }
                }
            } else if (building instanceof MineBuilding) {
                // For mines, if sources are provided, they must be defined.
                for (String srcName : building.getSources()) {
                    if (!buildings.containsKey(srcName)) {
                        throw new SimulationException("Building " + building.getName() + " has undefined source: " + srcName);
                    }
                }
            }
        }
    }
}