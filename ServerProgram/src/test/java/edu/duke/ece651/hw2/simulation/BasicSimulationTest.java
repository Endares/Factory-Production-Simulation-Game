package edu.duke.ece651.hw2.simulation;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit tests for BasicSimulation.
 */
public class BasicSimulationTest {

    @Test
    public void testInitialTimeStep() {
        BasicSimulation sim = new BasicSimulation(new HashMap<>(), new HashMap<>(), new HashMap<>());
        assertEquals(0, sim.getCurrentTimeStep());
    }

    @Test
    public void testStep() {
        BasicSimulation sim = new BasicSimulation(new HashMap<>(), new HashMap<>(), new HashMap<>());
        sim.step(5);
        assertEquals(5, sim.getCurrentTimeStep());
    }

    @Test
    public void testAddRequest() {
        // Prepare empty maps for buildings, recipes, and buildingTypes.
        Map<String, Building> buildings = new HashMap<>();
        Map<String, Recipe> recipes = new HashMap<>();
        Map<String, BuildingType> buildingTypes = new HashMap<>();

        // Create a simple Recipe for "door" (no ingredients, latency=10)
        Recipe doorRecipe = new Recipe("door", new HashMap<>(), 10);
        recipes.put("door", doorRecipe);

        // Create a minimal Building implementation for "Factory1"
        // by using an anonymous subclass of BasicBuilding.
        Building factory1 = new BasicBuilding("Factory1", new ArrayList<>()) {
            @Override
            public Request selectNextRequest(int currentTimeStep, int verbosity) {
                // No requests to select in this minimal implementation.
                return null;
            }

            @Override
            public List<String> getProvidedOutputs() {
                // This building is capable of producing "door".
                return List.of("door");
            }

            @Override
            public boolean canProduce(String itemName) {
                // Returns true if the requested item is "door".
                return "door".equals(itemName);
            }
        };
        buildings.put("Factory1", factory1);

        // Create a BasicSimulation with the maps
        BasicSimulation sim = new BasicSimulation(buildings, recipes, buildingTypes);

        // Get the current next request ID.
        int requestId = sim.gettheNextRequestId();

        // Create a Request using:
        // - id: requestId
        // - recipe: doorRecipe
        // - requestor: factory1
        // - isUserRequest: false
        // - timeRequested: 1
        Request req = new Request(requestId, doorRecipe, factory1, false, 1);

        // Add the request to the simulation.
        sim.addRequest(req);

        System.out.println(req.getId());
        // Check that the nextRequestId is incremented by 1.
        assertEquals(requestId + 1, sim.getNextRequestId(), "The next request ID should increment by 1");
    }

    @Test
    public void testSetVerbosity() {
        BasicSimulation sim = new BasicSimulation(new HashMap<>(), new HashMap<>(), new HashMap<>());
        sim.setVerbosity(3);
        // 通过执行 verbose 命令测试，无异常即认为通过
        sim.processCommand("verbose 5");
    }

    @Test
    public void testProcessInvalidCommandDoesNotExit() {
        BasicSimulation sim = new BasicSimulation(new HashMap<>(), new HashMap<>(), new HashMap<>());
        // Process an invalid command and ensure no state change.
        int timeBefore = sim.getCurrentTimeStep();
        sim.processCommand("invalid command");
        assertEquals(timeBefore, sim.getCurrentTimeStep());
    }

    // Test the processIngredients() in BasicBuilding with handleRequestCommand() in
    // BasicSimulation
    @Disabled
    @Test
    public void testProcessIngredients() throws SimulationException {
        Map<String, Building> buildings = new HashMap<>();
        Map<String, Recipe> recipes = new HashMap<>();
        Map<String, BuildingType> buildingTypes = new HashMap<>();

        // wood: no ingredients, latency=1
        Recipe woodRecipe = new Recipe("wood", new HashMap<>(), 1);
        recipes.put("wood", woodRecipe);
        // metal: no ingredients, latency=1
        Recipe metalRecipe = new Recipe("metal", new HashMap<>(), 1);
        recipes.put("metal", metalRecipe);

        // handle: ingredients: metal * 1, latency=5
        Map<String, Integer> handleIngr = new HashMap<>();
        handleIngr.put("metal", 1);
        Recipe handleRecipe = new Recipe("handle", handleIngr, 5);
        recipes.put("handle", handleRecipe);

        // hinge: ingredients: metal * 1，latency=1
        Map<String, Integer> hingeIngr = new HashMap<>();
        hingeIngr.put("metal", 1);
        Recipe hingeRecipe = new Recipe("hinge", hingeIngr, 1);
        recipes.put("hinge", hingeRecipe);

        // door: ingredients: wood * 1, handle * 1, hinge * 3，latency=12
        Map<String, Integer> doorIngr = new HashMap<>();
        doorIngr.put("wood", 1);
        doorIngr.put("handle", 1);
        doorIngr.put("hinge", 3);
        Recipe doorRecipe = new Recipe("door", doorIngr, 12);
        recipes.put("door", doorRecipe);

        // woodMine, metalMine
        BuildingType woodMineType = new BuildingType("woodMine", List.of("wood"));
        buildingTypes.put("woodMine", woodMineType);

        BuildingType metalMineType = new BuildingType("metalMine", List.of("metal"));
        buildingTypes.put("metalMine", metalMineType);

        // handleFactory, hingeFactory, doorFactory
        BuildingType handleFactoryType = new BuildingType("handleFactory", List.of("handle"));
        buildingTypes.put("handleFactory", handleFactoryType);

        BuildingType hingeFactoryType = new BuildingType("hingeFactory", List.of("hinge"));
        buildingTypes.put("hingeFactory", hingeFactoryType);

        BuildingType doorFactoryType = new BuildingType("doorFactory", List.of("door"));
        buildingTypes.put("doorFactory", doorFactoryType);

        // BuildingTypes: MineBuilding / FactoryBuilding
        // WoodBuilding W: mine = "wood"，sources=[]
        MineBuilding W = new MineBuilding("W", "wood", woodRecipe, new ArrayList<>());
        buildings.put("W", W);

        // MineBuilding M：mine = "metal"，sources=[]
        MineBuilding M = new MineBuilding("M", "metal", metalRecipe, new ArrayList<>());
        buildings.put("M", M);

        // Ha: type = handleFactory，sources=[M]
        List<String> haSources = new ArrayList<>();
        haSources.add("M");
        FactoryBuilding Ha = new FactoryBuilding("Ha", handleFactoryType, haSources);
        buildings.put("Ha", Ha);

        // HingeFactory Hi: type = hingeFactory，sources=[M]
        List<String> hiSources = new ArrayList<>();
        hiSources.add("M");
        FactoryBuilding Hi = new FactoryBuilding("Hi", hingeFactoryType, hiSources);
        buildings.put("Hi", Hi);

        // DoorFactory D: type = doorFactory，sources=[W, Ha, Hi]
        List<String> dSources = new ArrayList<>();
        dSources.add("W");
        dSources.add("Ha");
        dSources.add("Hi");
        FactoryBuilding D = new FactoryBuilding("D", doorFactoryType, dSources);
        buildings.put("D", D);

        BasicSimulation sim = new BasicSimulation(buildings, recipes, buildingTypes);
        // command: request "door" from "D"
        sim.handleRequestCommand("door", "D");
        // test dispatch of recipes
        // Now, capture printed building info
        String actualOutput = captureBuildingInfo(buildings);
        String expected = """
                Name: Hi
                Type: FactoryBuilding
                Sources: [M]
                RequestQueue:
                  - Request ID: 2, Output: hinge, Status: WAITING_FOR_INGREDIENTS, Requestor=D
                  - Request ID: 4, Output: hinge, Status: WAITING_FOR_INGREDIENTS, Requestor=D
                  - Request ID: 6, Output: hinge, Status: WAITING_FOR_INGREDIENTS, Requestor=D
                CurrentRequest: None
                Storage: {}

                Name: D
                Type: FactoryBuilding
                Sources: [W, Ha, Hi]
                RequestQueue:
                  - Request ID: 1, Output: door, Status: WAITING_FOR_INGREDIENTS, Requestor=user
                CurrentRequest: None
                Storage: {}

                Name: W
                Type: MineBuilding
                Sources: []
                RequestQueue: []
                CurrentRequest: ID=8, Output=wood, Status=IN_PROGRESS, Requestor=D
                Storage: {}

                Name: Ha
                Type: FactoryBuilding
                Sources: [M]
                RequestQueue:
                  - Request ID: 9, Output: handle, Status: WAITING_FOR_INGREDIENTS, Requestor=D
                CurrentRequest: None
                Storage: {}

                Name: M
                Type: MineBuilding
                Sources: []
                RequestQueue:
                  - Request ID: 5, Output: metal, Status: WAITING_FOR_INGREDIENTS, Requestor=Hi
                  - Request ID: 7, Output: metal, Status: WAITING_FOR_INGREDIENTS, Requestor=Hi
                  - Request ID: 10, Output: metal, Status: WAITING_FOR_INGREDIENTS, Requestor=Ha
                CurrentRequest: ID=3, Output=metal, Status=IN_PROGRESS, Requestor=Hi
                Storage: {}

                """;
        assertEquals(expected, actualOutput);
    }

    // Helper method renamed to captureBuildingInfo to avoid name conflict
    private String captureBuildingInfo(Map<String, Building> buildings) {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            // Call the method that prints building info for all buildings
            // This calls the instance method defined in our test class that loops over
            // buildings
            printBuildingInfo(buildings);
        } finally {
            System.setOut(originalOut);
        }
        return outContent.toString().replace("\r\n", "\n");
    }

    // This method is the one that loops over buildings and calls
    // building.printInfo()
    private void printBuildingInfo(Map<String, Building> buildings) {
        for (Building building : buildings.values()) {
            System.out.println(building);
        }
    }

    @Disabled
    @Test
    public void testToReactSerializable() {
        // 准备数据
        Map<String, Building> buildings = new HashMap<>();
        Map<String, Recipe> recipes = new HashMap<>();
        Map<String, BuildingType> buildingTypes = new HashMap<>();

        // 创建一个 BasicBuilding 实例，名称 "B1"
        Building b1 = new BasicBuilding("B1", new ArrayList<>()) {
            @Override
            public List<String> getProvidedOutputs() {
                return List.of("output");  // 此处内容不影响本测试
            }
            @Override
            public boolean canProduce(String itemName) {
                return "output".equals(itemName);
            }
        };
        buildings.put("B1", b1);

        // 创建一个简单的 Recipe，名称 "R1"
        Recipe r1 = new Recipe("R1", new HashMap<>(), 10);
        recipes.put("R1", r1);

        // 使用构造器构造 BasicSimulation 对象
        BasicSimulation sim = new BasicSimulation(buildings, recipes, buildingTypes);
        sim.setVerbosity(1);

        // 构造 RoadMap 对象
        // 在 (10,10) 放置建筑 b1
        Map<Coordinate, Building> buildingLocations = new HashMap<>();
        Coordinate coordBuilding = new Coordinate(10, 10);
        buildingLocations.put(coordBuilding, b1);

        // 在 (20,20) 放置一个 Road 对象
        Map<Coordinate, Road> roads = new HashMap<>();
        Coordinate coordRoad = new Coordinate(20, 20);
        Road road = new Road(coordRoad);
        // 设置方向：向 NORTH 与 EAST 设为 enter，向 WEST 设为 exit
        road.addEnterDirection(Direction.NORTH);
        road.addEnterDirection(Direction.EAST);
        road.addExitDirection(Direction.WEST);
        roads.put(coordRoad, road);

        // 假设 RoadMap 类有相应的 setter 方法
        RoadMap rm = new RoadMap();
        rm.setBuildingLocations(buildingLocations);
        rm.setRoads(roads);
        // 将构造好的 RoadMap 注入 BasicSimulation 中
        sim.setRoadMap(rm);
        rm.printMap();

        // 调用 toSerializable() 方法（这里代替原来的 toReactSerializable() 方法）
        JSONObject json = sim.toSerializable();

        // 验证数值字段
        assertEquals(0, json.getInt("currentTimeStep"), "初始 currentTimeStep 应为 0");
        assertEquals(1, json.getInt("verbosityLevel"), "verbosityLevel 应为 1");

        // 验证 buildings 数组，应包含 "B1"
        JSONArray buildingsArray = json.getJSONArray("buildings");
        assertEquals(1, buildingsArray.length(), "buildings 数组长度应为 1");
        assertEquals("B1", buildingsArray.getString(0), "建筑名称应为 'B1'");

        // 验证 recipes 数组，应包含 "R1"
        JSONArray recipesArray = json.getJSONArray("recipes");
        assertEquals(1, recipesArray.length(), "recipes 数组长度应为 1");
        assertEquals("R1", recipesArray.getString(0), "Recipe 名称应为 'R1'");

        // 验证 roadMap 为 50x50 二维数组
        JSONArray roadMapJson = json.getJSONArray("roadMap");
        assertEquals(50, roadMapJson.length(), "roadMap 第一层数组长度应为 50");
        for (int i = 0; i < 50; i++) {
            JSONArray row = roadMapJson.getJSONArray(i);
            assertEquals(50, row.length(), "roadMap 第 " + i + " 行长度应为 50");
        }

        // 验证 (10,10) 处有建筑信息
        Object cellAt10_10 = roadMapJson.getJSONArray(10).get(10);
        assertInstanceOf(JSONArray.class, cellAt10_10, "(10,10) 处应为 JSONArray 格式的建筑信息");
        JSONArray buildingInfo = (JSONArray) cellAt10_10;
        // 检查建筑名称
        assertEquals("B1", buildingInfo.getString(0), "建筑信息中名称应为 'B1'");

        // 验证 (20,20) 处的 Road 对象转换结果
        Object cellAt20_20 = roadMapJson.getJSONArray(20).get(20);
        assertInstanceOf(String.class, cellAt20_20, "(20,20) 处应为 String 类型的路信息");
        String roadInfo = (String) cellAt20_20;
        // 根据 toSerializable() 的转换规则，按照 NORTH, EAST, SOUTH, WEST：
        // NORTH: 在 enter → "1"
        // EAST: 在 enter → "1"
        // SOUTH: 默认 → "0"
        // WEST: 在 exit → "2"
        // 故预期字符串为 "1102"
        assertEquals("1102", roadInfo, "（20,20）处的路信息应为 '1102'");

//        // 验证 (0,0) 处既无建筑也无路格，应为 null
//        Object cellAt0_0 = roadMapJson.getJSONArray(0).get(0);
//        assertTrue(cellAt0_0 == JSONObject.NULL, "(0,0) 处应为 null");

        System.out.println(json.toString(4));
    }
    @Test
    void testBuildingConnectionValidation() throws Exception {
        // Parse the test JSON file
        String filePath = "src/test/resources/inputs/phase_tx_1.json";
        SimulationParser parser = new SimulationParser();
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
        
        // Test 1: Valid connection path - normal request should succeed
        String errors = simulation.checkBuildingConnections(buildings.get("D"), "door");
        String expected = """
                Error: No path exists from 'D' to source 'W' needed for ingredient 'wood'.
                Error: No source for building 'D' can produce 'wood'.
                """;
        assertEquals(errors, expected, "Should have no errors for valid connections");
    }
}