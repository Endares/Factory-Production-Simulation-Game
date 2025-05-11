package edu.duke.ece651.hw2.simulation;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Unit tests for SimulationParser.
 */
public class SimulationParserTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private SimulationParser parser = new SimulationParser();

    private File tempFile;

    // Utility method: creates a recipe ObjectNode.
    private static ObjectNode createRecipe(ObjectMapper mapper, String output, ObjectNode ingredients, int latency) {
        ObjectNode recipe = mapper.createObjectNode();
        recipe.put("output", output);
        recipe.set("ingredients", ingredients);
        recipe.put("latency", latency);
        return recipe;
    }

    // Utility method: creates a type ObjectNode.
    private static ObjectNode createType(ObjectMapper mapper, String name, String recipeName) {
        ObjectNode type = mapper.createObjectNode();
        type.put("name", name);
        ArrayNode recipes = mapper.createArrayNode();
        recipes.add(recipeName);
        type.set("recipes", recipes);
        return type;
    }

    // Utility method: creates a building ObjectNode (for factory).
    private static ObjectNode createBuilding(ObjectMapper mapper, String name, String type, ArrayNode sources) {
        ObjectNode b = mapper.createObjectNode();
        b.put("name", name);
        b.put("type", type);
        b.set("sources", sources);
        return b;
    }

    // Utility method: creates a mine building ObjectNode.
    private static ObjectNode createMine(ObjectMapper mapper, String name, String mine) {
        ObjectNode b = mapper.createObjectNode();
        b.put("name", name);
        b.put("mine", mine);
        b.set("sources", mapper.createArrayNode());
        return b;
    }

    // Utility method: constructs an ArrayNode from String items.
    private static ArrayNode array(ObjectMapper mapper, String... items) {
        ArrayNode array = mapper.createArrayNode();
        for (String item : items) {
            array.add(item);
        }
        return array;
    }

    // Utility method: constructs an ObjectNode from key-value pairs (only handling int and String).
    private static ObjectNode object(ObjectMapper mapper, Object... keyValues) {
        ObjectNode node = mapper.createObjectNode();
        for (int i = 0; i < keyValues.length; i += 2) {
            String k = (String) keyValues[i];
            Object v = keyValues[i + 1];
            if (v instanceof Integer) node.put(k, (Integer) v);
            else if (v instanceof String) node.put(k, (String) v);
        }
        return node;
    }

    // Create a valid JSON tree that includes recipes, types, and buildings.
    public static JsonNode createValidJson(ObjectMapper mapper) {
        ObjectNode root = mapper.createObjectNode();

        // recipes section
        ArrayNode recipes = mapper.createArrayNode();
        recipes.add(createRecipe(mapper, "door", object(mapper, "wood", 1, "handle", 1, "hinge", 3), 12));
        recipes.add(createRecipe(mapper, "handle", object(mapper, "metal", 1), 5));
        recipes.add(createRecipe(mapper, "hinge", object(mapper, "metal", 1), 1));
        recipes.add(createRecipe(mapper, "wood", mapper.createObjectNode(), 1));
        recipes.add(createRecipe(mapper, "metal", mapper.createObjectNode(), 1));
        root.set("recipes", recipes);

        // types section
        ArrayNode types = mapper.createArrayNode();
        types.add(createType(mapper, "door", "door"));
        types.add(createType(mapper, "handle", "handle"));
        types.add(createType(mapper, "hinge", "hinge"));
        root.set("types", types);

        // buildings section
        ArrayNode buildings = mapper.createArrayNode();
        buildings.add(createBuilding(mapper, "D", "door", array(mapper, "W", "Hi", "Ha")));
        buildings.add(createBuilding(mapper, "Ha", "handle", array(mapper, "M")));
        buildings.add(createBuilding(mapper, "Hi", "hinge", array(mapper, "M")));
        buildings.add(createMine(mapper, "W", "wood"));
        buildings.add(createMine(mapper, "M", "metal"));
        root.set("buildings", buildings);

        return root;
    }

    @BeforeEach
    public void setupTempFile() throws IOException {
        // Create a temporary file with valid JSON content.
        tempFile = File.createTempFile("simtest", ".json");
        JsonNode validJson = createValidJson(mapper);
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(validJson.toString());
        }
    }

    @AfterEach
    public void cleanupTempFile() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    public void testParseJsonFile() throws IOException {
        // Use the temporary file to test parseJsonFile.
        JsonNode parsed = parser.parseJsonFile(tempFile.getAbsolutePath());
        assertNotNull(parsed);
        assertTrue(parsed.has("recipes"));
        assertTrue(parsed.has("types"));
        assertTrue(parsed.has("buildings"));
    }

    @Test
    public void testParseRecipes() throws SimulationException {
        JsonNode json = createValidJson(mapper);
        Map<String, Recipe> recipes = parser.parseRecipes(json);
        assertEquals(5, recipes.size());
        assertTrue(recipes.containsKey("door"));
        Recipe doorRecipe = recipes.get("door");
        assertEquals(12, doorRecipe.getLatency());
        // door recipe should have 3 ingredients: wood, handle, hinge.
        assertEquals(3, doorRecipe.getIngredients().size());
    }

    @Test
    public void testParseTypes() throws SimulationException {
        JsonNode json = createValidJson(mapper);
        Map<String, Recipe> recipes = parser.parseRecipes(json);
        Map<String, BuildingType> types = parser.parseTypes(json, recipes);
        assertEquals(3, types.size());
        assertTrue(types.containsKey("door"));
        BuildingType doorType = types.get("door");
        // getRecipes() returns a List<String>
        assertEquals(1, doorType.getRecipes().size());
        assertEquals("door", doorType.getRecipes().getFirst());
    }

    @Test
    public void testParseBuildings() throws SimulationException {
        JsonNode json = createValidJson(mapper);
        Map<String, Recipe> recipes = parser.parseRecipes(json);
        Map<String, BuildingType> types = parser.parseTypes(json, recipes);
        Map<String, Building> buildings = parser.parseBuildings(json, types, recipes);
        assertEquals(5, buildings.size());
        assertTrue(buildings.containsKey("D"));
        Building d = buildings.get("D");
        assertInstanceOf(FactoryBuilding.class, d);
        Building m = buildings.get("M");
        assertInstanceOf(MineBuilding.class, m);
    }

    @Test
    public void testValidateInputValid() throws SimulationException {
        JsonNode json = createValidJson(mapper);
        Map<String, Recipe> recipes = parser.parseRecipes(json);
        Map<String, BuildingType> types = parser.parseTypes(json, recipes);
        Map<String, Building> buildings = parser.parseBuildings(json, types, recipes);
        // Should not throw any exception.
        parser.validateInput(buildings, recipes);
    }

    @Test
    public void testParseRecipesMissingField() {
        ObjectNode json = mapper.createObjectNode();
        ArrayNode recipes = mapper.createArrayNode();
        // Add an empty object missing required fields.
        recipes.add(mapper.createObjectNode());
        json.set("recipes", recipes);
        SimulationException ex = assertThrows(SimulationException.class, () -> parser.parseRecipes(json));
        assertTrue(ex.getMessage().contains("Recipe missing required field"));
    }

    @Test
    public void testParseTypesMissingField() {
        ObjectNode json = mapper.createObjectNode();
        ArrayNode types = mapper.createArrayNode();
        ObjectNode type = mapper.createObjectNode();
        type.put("name", "testType"); // missing "recipes"
        types.add(type);
        json.set("types", types);
        Map<String, Recipe> dummyRecipes = new HashMap<>();
        SimulationException ex = assertThrows(SimulationException.class, () -> parser.parseTypes(json, dummyRecipes));
        assertTrue(ex.getMessage().contains("Type missing required field"));
    }

    @Test
    public void testParseBuildingsMissingName() {
        ObjectNode json = mapper.createObjectNode();
        ArrayNode buildings = mapper.createArrayNode();
        ObjectNode building = mapper.createObjectNode();
        building.put("type", "test"); // missing "name"
        buildings.add(building);
        json.set("buildings", buildings);
        Map<String, Recipe> dummyRecipes = new HashMap<>();
        Map<String, BuildingType> dummyTypes = new HashMap<>();
        SimulationException ex = assertThrows(SimulationException.class, () -> parser.parseBuildings(json, dummyTypes, dummyRecipes));
        assertTrue(ex.getMessage().contains("Building missing 'name' field"));
    }

    @Test
    public void testParseBuildingsBothTypeAndMine() {
        ObjectNode json = mapper.createObjectNode();
        ArrayNode buildings = mapper.createArrayNode();
        ObjectNode building = mapper.createObjectNode();
        building.put("name", "B1");
        building.put("type", "test");
        building.put("mine", "testRecipe");
        buildings.add(building);
        json.set("buildings", buildings);
        Map<String, Recipe> dummyRecipes = new HashMap<>();
        dummyRecipes.put("testRecipe", new Recipe("testRecipe", new HashMap<>(), 1));
        Map<String, BuildingType> dummyTypes = new HashMap<>();
        SimulationException ex = assertThrows(SimulationException.class, () -> parser.parseBuildings(json, dummyTypes, dummyRecipes));
        assertTrue(ex.getMessage().contains("cannot have both 'type' and 'mine'"));
    }

    @Test
    public void testValidateInputUndefinedIngredient() throws SimulationException {
        ObjectNode json = (ObjectNode) createValidJson(mapper);
        // Modify first recipe to include an undefined ingredient.
        ArrayNode recipesArray = (ArrayNode) json.get("recipes");
        ObjectNode recipe = (ObjectNode) recipesArray.get(0);
        ObjectNode ingredients = (ObjectNode) recipe.get("ingredients");
        ingredients.put("undefinedIngredient", 1);
        Map<String, Recipe> recipes = parser.parseRecipes(json);
        SimulationException ex = assertThrows(SimulationException.class,
                () -> parser.validateInput(new HashMap<>(), recipes));
        assertTrue(ex.getMessage().contains("has undefined ingredient"));
    }

    @Test
    public void testParseRecipeWithZeroLatency() {
        ObjectNode json = mapper.createObjectNode();
        ArrayNode recipes = mapper.createArrayNode();
        ObjectNode recipe = mapper.createObjectNode();
        recipe.put("output", "test");
        recipe.set("ingredients", mapper.createObjectNode());
        recipe.put("latency", 0); // Invalid latency
        recipes.add(recipe);
        json.set("recipes", recipes);
        SimulationException ex = assertThrows(SimulationException.class, () -> parser.parseRecipes(json));
        assertTrue(ex.getMessage().contains("Latency must be >= 1"));
    }

    @Test
    public void testParseRecipeWithDuplicateOutput() {
        ObjectNode json = mapper.createObjectNode();
        ArrayNode recipes = mapper.createArrayNode();

        ObjectNode recipe1 = mapper.createObjectNode();
        recipe1.put("output", "dup");
        recipe1.set("ingredients", mapper.createObjectNode());
        recipe1.put("latency", 1);

        ObjectNode recipe2 = mapper.createObjectNode();
        recipe2.put("output", "dup");
        recipe2.set("ingredients", mapper.createObjectNode());
        recipe2.put("latency", 2);

        recipes.add(recipe1);
        recipes.add(recipe2);
        json.set("recipes", recipes);

        SimulationException ex = assertThrows(SimulationException.class, () -> parser.parseRecipes(json));
        assertTrue(ex.getMessage().contains("Duplicate recipe name"));
    }

    @Test
    public void testParseTypesRecipesNotArray() {
        // Test scenario where the "recipes" field in a type is not an array.
        ObjectNode json = mapper.createObjectNode();
        ArrayNode types = mapper.createArrayNode();
        ObjectNode type = mapper.createObjectNode();
        type.put("name", "TestType");
        // Set "recipes" as a string instead of an array.
        type.put("recipes", "not an array");
        types.add(type);
        json.set("types", types);
        Map<String, Recipe> dummyRecipes = new HashMap<>();
        SimulationException ex = assertThrows(SimulationException.class, () -> parser.parseTypes(json, dummyRecipes));
        assertTrue(ex.getMessage().contains("Type recipes field is not an array"));
    }

    @Test
    public void testParseBuildingsTypesFieldNotArray() {
        // Test scenario where "buildings" field is not an array.
        ObjectNode json = mapper.createObjectNode();
        json.put("buildings", "not an array");
        Map<String, Recipe> dummyRecipes = new HashMap<>();
        Map<String, BuildingType> dummyTypes = new HashMap<>();
        SimulationException ex = assertThrows(SimulationException.class, () -> parser.parseBuildings(json, dummyTypes, dummyRecipes));
        assertTrue(ex.getMessage().contains("'buildings' field is not an array"));
    }

    // 新测试：解析一个正确的 StorageBuilding，验证各字段是否正确
    @Test
    public void testParseStorageBuildingValid() throws SimulationException {
        // 构造 JSON 根节点
        ObjectNode root = mapper.createObjectNode();

        // recipes 部分：为方便，不需要存储建筑参与生产，构造一个 dummy recipe
        ArrayNode recipesArray = mapper.createArrayNode();
        recipesArray.add(createRecipe(mapper, "dummy", mapper.createObjectNode(), 1));
        root.set("recipes", recipesArray);

        // types 部分为空数组即可
        ArrayNode typesArray = mapper.createArrayNode();
        root.set("types", typesArray);

        // buildings 部分：增加一个 StorageBuilding
        ArrayNode buildingsArray = mapper.createArrayNode();
        ObjectNode storageNode = mapper.createObjectNode();
        storageNode.put("name", "Bolt Storage");
        storageNode.put("stores", "bolt");
        storageNode.put("capacity", 100);
        storageNode.put("priority", 1.7);
        storageNode.set("sources", array(mapper, "Bolt Factory 1", "Bolt Factory 7", "Bolt Factory 33"));
        buildingsArray.add(storageNode);
        root.set("buildings", buildingsArray);

        Map<String, Recipe> recipes = parser.parseRecipes(root);
        Map<String, BuildingType> types = parser.parseTypes(root, recipes);
        Map<String, Building> buildings = parser.parseBuildings(root, types, recipes);

        assertTrue(buildings.containsKey("Bolt Storage"));
        Building storageBuilding = buildings.get("Bolt Storage");
        assertInstanceOf(StorageBuilding.class, storageBuilding);
        StorageBuilding sb = (StorageBuilding) storageBuilding;
        assertEquals("bolt", sb.getStoredItem());
        assertEquals(100, sb.getCapacity());
        assertEquals(1.7f, sb.getPriority());
        // 验证 sources
        List<String> expectedSources = Arrays.asList("Bolt Factory 1", "Bolt Factory 7", "Bolt Factory 33");
        assertEquals(expectedSources, sb.getSources());
    }

    // 新测试：StorageBuilding 缺失 capacity 字段，应抛出异常
    @Test
    public void testParseStorageBuildingMissingCapacity() {
        ObjectNode root = mapper.createObjectNode();

        // 创建 dummy recipe 部分
        ArrayNode recipesArray = mapper.createArrayNode();
        recipesArray.add(createRecipe(mapper, "dummy", mapper.createObjectNode(), 1));
        root.set("recipes", recipesArray);

        // types 空数组即可
        ArrayNode typesArray = mapper.createArrayNode();
        root.set("types", typesArray);

        // buildings 部分包含 StorageBuilding，但缺少 capacity
        ArrayNode buildingsArray = mapper.createArrayNode();
        ObjectNode storageNode = mapper.createObjectNode();
        storageNode.put("name", "Bolt Storage");
        storageNode.put("stores", "bolt");
        storageNode.put("priority", 1.7);
        storageNode.set("sources", array(mapper, "Bolt Factory 1", "Bolt Factory 7"));
        buildingsArray.add(storageNode);
        root.set("buildings", buildingsArray);

        SimulationException ex = assertThrows(SimulationException.class, () -> parser.parseBuildings(root, parser.parseTypes(root, parser.parseRecipes(root)), parser.parseRecipes(root)));
        assertTrue(ex.getMessage().contains("must have 'capacity'"));
    }

    // 新测试：StorageBuilding 缺失 priority 字段，应抛出异常
    @Test
    public void testParseStorageBuildingMissingPriority() {
        ObjectNode root = mapper.createObjectNode();

        // dummy recipe 部分
        ArrayNode recipesArray = mapper.createArrayNode();
        recipesArray.add(createRecipe(mapper, "dummy", mapper.createObjectNode(), 1));
        root.set("recipes", recipesArray);

        ArrayNode typesArray = mapper.createArrayNode();
        root.set("types", typesArray);

        // buildings 部分：StorageBuilding 缺失 priority
        ArrayNode buildingsArray = mapper.createArrayNode();
        ObjectNode storageNode = mapper.createObjectNode();
        storageNode.put("name", "Bolt Storage");
        storageNode.put("stores", "bolt");
        storageNode.put("capacity", 100);
        storageNode.set("sources", array(mapper, "Bolt Factory 1", "Bolt Factory 7"));
        buildingsArray.add(storageNode);
        root.set("buildings", buildingsArray);

        SimulationException ex = assertThrows(SimulationException.class, () -> parser.parseBuildings(root, parser.parseTypes(root, parser.parseRecipes(root)), parser.parseRecipes(root)));
        assertTrue(ex.getMessage().contains("must have 'capacity' and 'priority'"));
    }

    // 新测试：StorageBuilding 同时存在 "stores" 与 "type" 字段，应抛出异常
    @Test
    public void testParseStorageBuildingWithStoresAndType() {
        ObjectNode root = mapper.createObjectNode();

        // dummy recipe 部分
        ArrayNode recipesArray = mapper.createArrayNode();
        recipesArray.add(createRecipe(mapper, "dummy", mapper.createObjectNode(), 1));
        root.set("recipes", recipesArray);

        ArrayNode typesArray = mapper.createArrayNode();
        // 添加一个 dummy type
        typesArray.add(createType(mapper, "dummyType", "dummy"));
        root.set("types", typesArray);

        // buildings 部分：含有 stores 与 type 字段
        ArrayNode buildingsArray = mapper.createArrayNode();
        ObjectNode building = mapper.createObjectNode();
        building.put("name", "Bolt Storage");
        building.put("stores", "bolt");
        building.put("type", "dummyType");
        building.put("capacity", 100);
        building.put("priority", 1.7);
        building.set("sources", array(mapper, "Some Factory"));
        buildingsArray.add(building);
        root.set("buildings", buildingsArray);

        SimulationException ex = assertThrows(SimulationException.class, () -> parser.parseBuildings(root, parser.parseTypes(root, parser.parseRecipes(root)), parser.parseRecipes(root)));
        assertTrue(ex.getMessage().contains("cannot have both 'stores' and 'type/mine'"));
    }

    // 新测试：在包含 StorageBuilding 的 JSON 中，多种建筑共存，检查解析后 StorageBuilding 对象及其坐标分配
    @Test
    public void testParseBuildingsWithMultipleStorageBuildings() throws SimulationException {
        ObjectNode root = mapper.createObjectNode();

        // recipes 部分（dummy recipe即可）
        ArrayNode recipesArray = mapper.createArrayNode();
        recipesArray.add(createRecipe(mapper, "dummy", mapper.createObjectNode(), 1));
        root.set("recipes", recipesArray);

        // types 部分留空
        ArrayNode typesArray = mapper.createArrayNode();
        root.set("types", typesArray);

        // buildings 部分：一个 StorageBuilding 和两个 MineBuilding
        ArrayNode buildingsArray = mapper.createArrayNode();
        // Storage building
        ObjectNode storageNode1 = mapper.createObjectNode();
        storageNode1.put("name", "Bolt Storage");
        storageNode1.put("stores", "bolt");
        storageNode1.put("capacity", 150);
        storageNode1.put("priority", 2.0);
        storageNode1.set("sources", array(mapper, "Factory X", "Factory Y"));
        // Mine building
        ObjectNode mineNode1 = mapper.createObjectNode();
        mineNode1.put("name", "Bolt Mine");
        mineNode1.put("mine", "bolt");
        mineNode1.set("sources", mapper.createArrayNode());
        // Mine building 2
        ObjectNode mineNode2 = mapper.createObjectNode();
        mineNode2.put("name", "Metal Mine");
        mineNode2.put("mine", "metal");
        mineNode2.set("sources", mapper.createArrayNode());
        buildingsArray.add(storageNode1);
        buildingsArray.add(mineNode1);
        buildingsArray.add(mineNode2);
        root.set("buildings", buildingsArray);

        // 为使 MineBuilding 解析不报错，需要 dummy recipes：
        // 添加 bolt 和 metal recipes
        ObjectNode boltRecipe = createRecipe(mapper, "bolt", mapper.createObjectNode(), 1);
        ObjectNode metalRecipe = createRecipe(mapper, "metal", mapper.createObjectNode(), 1);
        recipesArray.add(boltRecipe);
        recipesArray.add(metalRecipe);

        Map<String, Recipe> recipes = parser.parseRecipes(root);
        Map<String, BuildingType> types = parser.parseTypes(root, recipes);
        Map<String, Building> buildings = parser.parseBuildings(root, types, recipes);

        // 检查总建筑数
        assertEquals(3, buildings.size());
        // 验证 StorageBuilding
        assertTrue(buildings.containsKey("Bolt Storage"));
        Building storage = buildings.get("Bolt Storage");
        assertInstanceOf(StorageBuilding.class, storage);
        // 验证 MineBuildings
        assertTrue(buildings.containsKey("Bolt Mine"));
        assertTrue(buildings.containsKey("Metal Mine"));

        // 检查坐标是否都已经分配（非 null）
        for (Building b : buildings.values()) {
            assertNotNull(b.getLocation(), "Building " + b.getName() + " should have a location assigned.");
        }
    }

    // 新测试：验证 StorageBuilding 的 toString 方法内容包含存储、容量、优先级等信息（内容可做部分匹配）。
    @Test
    public void testStorageBuildingToString() throws SimulationException {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode recipesArray = mapper.createArrayNode();
        recipesArray.add(createRecipe(mapper, "dummy", mapper.createObjectNode(), 1));
        root.set("recipes", recipesArray);
        ArrayNode typesArray = mapper.createArrayNode();
        root.set("types", typesArray);
        ArrayNode buildingsArray = mapper.createArrayNode();
        ObjectNode storageNode = mapper.createObjectNode();
        storageNode.put("name", "Bolt Storage");
        storageNode.put("stores", "bolt");
        storageNode.put("capacity", 200);
        storageNode.put("priority", 2.5);
        storageNode.set("sources", array(mapper, "Factory A", "Factory B"));
        buildingsArray.add(storageNode);
        root.set("buildings", buildingsArray);

        Map<String, Recipe> recipes = parser.parseRecipes(root);
        Map<String, BuildingType> types = parser.parseTypes(root, recipes);
        Map<String, Building> buildings = parser.parseBuildings(root, types, recipes);
        StorageBuilding sb = (StorageBuilding) buildings.get("Bolt Storage");

        String info = sb.toString();
//        System.out.println(info);
        assertTrue(info.contains("Bolt Storage"));
        assertTrue(info.contains("Storage"));
        assertTrue(info.contains("200"));
        assertTrue(info.contains("2.5"));
    }

    /**
     * Test parsing a minimal DroneBuilding entry: only name, x and y.
     */
    @Test
    public void testParseDroneBuildingValid() throws SimulationException {
        ObjectNode root = mapper.createObjectNode();
        root.set("recipes", mapper.createArrayNode());
        root.set("types", mapper.createArrayNode());
        ArrayNode buildings = mapper.createArrayNode();
        ObjectNode drone = mapper.createObjectNode();
        drone.put("name", "DroneHub");
        drone.put("x", 5);
        drone.put("y", 10);
        buildings.add(drone);
        root.set("buildings", buildings);

        Map<String, Recipe> recipes = parser.parseRecipes(root);
        Map<String, BuildingType> types = parser.parseTypes(root, recipes);
        Map<String, Building> builds = parser.parseBuildings(root, types, recipes);

        assertEquals(1, builds.size());
        assertInstanceOf(DroneBuilding.class, builds.get("DroneHub"));
        DroneBuilding db = (DroneBuilding) builds.get("DroneHub");
        assertEquals(new Coordinate(5, 10), db.getLocation());
        assertEquals(0, db.getDroneNumber(), "New DroneBuilding should start with zero drones");
    }

    /**
     * Test missing x or y for DroneBuilding triggers exception.
     */
    @Test
    public void testParseDroneBuildingMissingCoordinate() {
        ObjectNode root = mapper.createObjectNode();
        root.set("recipes", mapper.createArrayNode());
        root.set("types", mapper.createArrayNode());
        ArrayNode buildings = mapper.createArrayNode();
        ObjectNode noX = mapper.createObjectNode();
        noX.put("name", "D1");
        noX.put("y", 3);
        buildings.add(noX);
        ObjectNode noY = mapper.createObjectNode();
        noY.put("name", "D2");
        noY.put("x", 4);
        buildings.add(noY);
        root.set("buildings", buildings);

        SimulationException ex1 = assertThrows(SimulationException.class, () ->
                parser.parseBuildings(root, parser.parseTypes(root, parser.parseRecipes(root)), parser.parseRecipes(root))
        );
        assertTrue(ex1.getMessage().contains("must have either 'type', 'mine', or 'stores' field, or be a Drone Port Building"));

        // also ensure that completely empty coords fail
        ObjectNode empty = mapper.createObjectNode();
        empty.put("name", "D3");
        buildings.removeAll();
        buildings.add(empty);
        root.set("buildings", buildings);
        SimulationException ex2 = assertThrows(SimulationException.class, () ->
                parser.parseBuildings(root, parser.parseTypes(root, parser.parseRecipes(root)), parser.parseRecipes(root))
        );
        assertTrue(ex2.getMessage().contains("must have either 'type', 'mine', or 'stores' field, or be a Drone Port Building"));
    }

    /**
     * Test that DroneBuilding without any extra source list still parses.
     */
    @Test
    public void testParseDroneBuildingIgnoresSources() throws SimulationException {
        ObjectNode root = mapper.createObjectNode();
        root.set("recipes", mapper.createArrayNode());
        root.set("types", mapper.createArrayNode());
        ArrayNode buildings = mapper.createArrayNode();
        ObjectNode drone = mapper.createObjectNode();
        drone.put("name", "DroneA");
        drone.put("x", 0);
        drone.put("y", 0);
        ArrayNode src = mapper.createArrayNode();
        src.add("Factory1");
        drone.set("sources", src);  // even if sources provided, DroneBuilding ctor ignores it
        buildings.add(drone);
        root.set("buildings", buildings);

        Map<String, Recipe> recipes = parser.parseRecipes(root);
        Map<String, BuildingType> types = parser.parseTypes(root, recipes);
        Map<String, Building> builds = parser.parseBuildings(root, types, recipes);

        assertEquals(1, builds.size());
        assertInstanceOf(DroneBuilding.class, builds.get("DroneA"));
        // sources should be empty list internally
        assertTrue(builds.get("DroneA").getSources().isEmpty());
    }
}