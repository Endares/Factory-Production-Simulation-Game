package edu.duke.ece651.hw2.simulation;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

public class RoadMapTest {
    /**
     * Helper method that parses the JSON configuration, constructs the simulation,
     * builds the roadmap, and prints the map. It returns the captured output from printing.
     *
     * @param jsonStr the JSON configuration string
     * @return the output produced by printing the roadmap
     * @throws Exception if an error occurs during JSON processing or simulation building
     */
    private String runPrintMapTest(String jsonStr) throws Exception {
        // Parse the JSON string.
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(jsonStr);
        SimulationParser parser = new SimulationParser();

        // Parse recipes, types, and buildings.
        Map<String, Recipe> recipes = parser.parseRecipes(json);
        Map<String, BuildingType> types = parser.parseTypes(json, recipes);
        Map<String, Building> buildings = parser.parseBuildings(json, types, recipes);

        BasicSimulation sim = new BasicSimulation(buildings, recipes, types);
        parser.parseConnections(json, sim);

        // Build the RoadMap and add all buildings.
        RoadMap roadMap = sim.getRoadMap();
        for (Building building : buildings.values()) {
            roadMap.addBuilding(building);
        }

        // Print the map.
        return roadMap.printMap();
    }

    @Test
    public void testPrintMap() throws Exception {
        String jsonStr = """
                {
                    "types" : [
                        { "name" : "door", "recipes" : [ "door" ] },
                        { "name" : "handle", "recipes" : [ "handle" ] },
                        { "name" : "hinge", "recipes" : [ "hinge" ] }
                    ],
                    "buildings" : [
                        { "name" : "D", "type" : "door", "x" : 1, "y" : 1, "sources" : [ "W", "Hi", "Ha" ] },
                        { "name" : "Ha", "type" : "handle", "x" : 7, "y" : 13, "sources" : [ "M" ] },
                        { "name" : "Hi", "type" : "hinge", "x" : 8, "y" : 5, "sources" : [ "M" ] },
                        { "name" : "W", "mine" : "wood", "x" : 4, "y" : 2, "sources" : [] },
                        { "name" : "M", "mine" : "metal", "x" : 2, "y" : 14, "sources" : [] }
                    ],
                    "recipes" : [
                        { "output" : "door", "ingredients" : { "wood" : 1, "handle" : 1, "hinge" : 3 }, "latency" : 12 },
                        { "output" : "handle", "ingredients" : { "metal" : 1 }, "latency" : 5 },
                        { "output" : "hinge", "ingredients" : { "metal" : 1 }, "latency" : 1 },
                        { "output" : "wood", "ingredients" : {}, "latency" : 1 },
                        { "output" : "metal", "ingredients" : {}, "latency" : 1 }
                    ],
                    "connections": [
                        { "source": "M", "destination": "Ha" },
                        { "source": "D", "destination": "W" },
                        { "source": "D", "destination": "Hi" }
                    ]
                }""";
        String output = runPrintMapTest(jsonStr);
        // Verify that the printed output is not empty.
        assertFalse(output.isEmpty(), "Printed map output should not be empty");
        // Additional regression check: verify the output includes expected building information.
        assertTrue(output.contains("D"), "Output should contain information about building D");
//        System.out.println("Printed Map1:\n" + output);
    }

    @Test
    public void testPrintMap2() throws Exception {
        String jsonStr = """
                {
                    "types" : [
                        { "name" : "door", "recipes" : [ "door" ] },
                        { "name" : "handle", "recipes" : [ "handle" ] },
                        { "name" : "hinge", "recipes" : [ "hinge" ] }
                    ],
                    "buildings" : [
                        { "name" : "D", "type" : "door", "x" : 1, "y" : 1, "sources" : [ "W", "Hi", "Ha" ] },
                        { "name" : "Ha", "type" : "handle", "x" : 7, "y" : 13, "sources" : [ "M" ] },
                        { "name" : "Hi", "type" : "hinge", "x" : 8, "y" : 5, "sources" : [ "M" ] },
                        { "name" : "W", "mine" : "wood", "x" : 4, "y" : 2, "sources" : [] },
                        { "name" : "M", "mine" : "metal", "x" : 2, "y" : 14, "sources" : [] }
                    ],
                    "recipes" : [
                        { "output" : "door", "ingredients" : { "wood" : 1, "handle" : 1, "hinge" : 3 }, "latency" : 12 },
                        { "output" : "handle", "ingredients" : { "metal" : 1 }, "latency" : 5 },
                        { "output" : "hinge", "ingredients" : { "metal" : 1 }, "latency" : 1 },
                        { "output" : "wood", "ingredients" : {}, "latency" : 1 },
                        { "output" : "metal", "ingredients" : {}, "latency" : 1 }
                    ],
                    "connections": [
                        { "source": "M", "destination": "Ha" },
                        { "source": "W", "destination": "D" },
                        { "source": "Hi", "destination": "D" }
                    ]
                }""";
        String output = runPrintMapTest(jsonStr);
        assertFalse(output.isEmpty(), "Printed map output should not be empty");
        // Additional assertion: check that the output includes expected information about building Ha.
        assertTrue(output.contains("Ha"), "Output should contain information about building Ha");
//        System.out.println("Printed Map2:\n" + output);
    }

    @Test
    public void testLocationAssign() throws Exception {
        // This test deliberately omits the x and y coordinates to check that the simulation automatically assigns locations.
        String jsonStr = """
                {
                    "types" : [
                        { "name" : "door", "recipes" : [ "door" ] },
                        { "name" : "handle", "recipes" : [ "handle" ] },
                        { "name" : "hinge", "recipes" : [ "hinge" ] }
                    ],
                    "buildings" : [
                        { "name" : "D", "type" : "door", "sources" : [ "W", "Hi", "Ha" ] },
                        { "name" : "Ha", "type" : "handle", "sources" : [ "M" ] },
                        { "name" : "Hi", "type" : "hinge", "sources" : [ "M" ] },
                        { "name" : "W", "mine" : "wood", "sources" : [] },
                        { "name" : "M", "mine" : "metal", "sources" : [] }
                    ],
                    "recipes" : [
                        { "output" : "door", "ingredients" : { "wood" : 1, "handle" : 1, "hinge" : 3 }, "latency" : 12 },
                        { "output" : "handle", "ingredients" : { "metal" : 1 }, "latency" : 5 },
                        { "output" : "hinge", "ingredients" : { "metal" : 1 }, "latency" : 1 },
                        { "output" : "wood", "ingredients" : {}, "latency" : 1 },
                        { "output" : "metal", "ingredients" : {}, "latency" : 1 }
                    ],
                    "connections": [
                        { "source": "M", "destination": "Ha" },
                        { "source": "W", "destination": "D" },
                        { "source": "Hi", "destination": "D" }
                    ]
                }""";
        String output = runPrintMapTest(jsonStr);
        assertFalse(output.isEmpty(), "Printed map output should not be empty");
        // Further assertions can be added here if the simulation prints assigned locations.
//        System.out.println("Printed Map3:\n" + output);
    }

    /**
     * Test the correctness of storage input, location and connection
    */
    @Test
    public void testStorageBuilding() throws Exception {
        String jsonStr = """
                {
                    "types" : [
                        { "name" : "door", "recipes" : [ "door" ] },
                        { "name" : "handle", "recipes" : [ "handle" ] },
                        { "name" : "hinge", "recipes" : [ "hinge" ] }
                    ],
                    "buildings" : [
                        { "name" : "D", "type" : "door", "x" : 1, "y" : 1, "sources" : [ "W", "Hi", "Ha" ] },
                        { "name" : "Ha", "type" : "handle", "x" : 7, "y" : 13, "sources" : [ "M" ] },
                        { "name" : "Hi", "type" : "hinge", "sources" : [ "M" ] },
                        { "name" : "W", "mine" : "wood", "x" : 4, "y" : 2, "sources" : [] },
                        { "name" : "M", "mine" : "metal", "sources" : [] },
                        { "name" : "S1", "stores" : "hinge", "x" : 2, "y" : 14, "capacity" : 100, "priority" : 1.7, "sources" : ["Hi"] }
                    ],
                    "recipes" : [
                        { "output" : "door", "ingredients" : { "wood" : 1, "handle" : 1, "hinge" : 3 }, "latency" : 12 },
                        { "output" : "handle", "ingredients" : { "metal" : 1 }, "latency" : 5 },
                        { "output" : "hinge", "ingredients" : { "metal" : 1 }, "latency" : 1 },
                        { "output" : "wood", "ingredients" : {}, "latency" : 1 },
                        { "output" : "metal", "ingredients" : {}, "latency" : 1 }
                    ],
                    "connections": [
                        { "source": "M", "destination": "Ha" },
                        { "source": "W", "destination": "D" },
                        { "source": "Hi", "destination": "S1" }
                    ]
                }""";
        String output = runPrintMapTest(jsonStr);
        assertFalse(output.isEmpty(), "Printed map output should not be empty");
        // 可在此进一步断言自动分配坐标或特定标识（比如打印的信息中是否提及“location”或具体位置编号）
//        System.out.println("Printed Map4:\n" + output);
    }

    // ========== getShortestDistance 相关测试 ==========

    @Test
    public void testGetShortestDistance_Adjacent_NoRoads() {
        RoadMap rm = new RoadMap();
        FactoryBuilding a = new FactoryBuilding("A", new BuildingType("t", List.of()), List.of());
        FactoryBuilding b = new FactoryBuilding("B", new BuildingType("t", List.of()), List.of());
        a.setLocation(new Coordinate(0, 0));
        b.setLocation(new Coordinate(0, 1));
        rm.addBuilding(a);
        rm.addBuilding(b);

        // 直接相邻，无需路格
        assertEquals(0, rm.getShortestDistance(a, b));
    }

    @Test
    public void testGetShortestDistance_NoPath() {
        RoadMap rm = new RoadMap();
        FactoryBuilding a = new FactoryBuilding("A", new BuildingType("t", List.of()), List.of());
        FactoryBuilding b = new FactoryBuilding("B", new BuildingType("t", List.of()), List.of());
        a.setLocation(new Coordinate(0, 0));
        b.setLocation(new Coordinate(2, 2));
        rm.addBuilding(a);
        rm.addBuilding(b);
        // 没有路格，且不相邻
        assertEquals(-1, rm.getShortestDistance(a, b));
    }

    @Test
    public void testGetShortestDistance_SimpleRoad() {
        RoadMap rm = new RoadMap();
        FactoryBuilding a = new FactoryBuilding("A", new BuildingType("t", List.of()), List.of());
        FactoryBuilding b = new FactoryBuilding("B", new BuildingType("t", List.of()), List.of());
        a.setLocation(new Coordinate(0, 0));
        b.setLocation(new Coordinate(0, 2));
        rm.addBuilding(a);
        rm.addBuilding(b);
        // 在 (0,1) 放一条双向路
        Road road = new Road(new Coordinate(0, 1));
        road.addEnterDirection(Direction.NORTH);
        road.addExitDirection(Direction.SOUTH);
        road.addEnterDirection(Direction.SOUTH);
        road.addExitDirection(Direction.NORTH);
        rm.getRoads().put(new Coordinate(0, 1), road);

        assertEquals(2, rm.getShortestDistance(a, b));
    }

    @Test
    public void testGetShortestDistance_DirectedBlock() {
        RoadMap rm = new RoadMap();
        FactoryBuilding a = new FactoryBuilding("A", new BuildingType("t", List.of()), List.of());
        FactoryBuilding b = new FactoryBuilding("B", new BuildingType("t", List.of()), List.of());
        a.setLocation(new Coordinate(0, 0));
        b.setLocation(new Coordinate(0, 2));
        rm.addBuilding(a);
        rm.addBuilding(b);
        Road r = new Road(new Coordinate(0, 1));
        r.addEnterDirection(Direction.NORTH);
        r.addExitDirection(Direction.SOUTH);
        rm.getRoads().put(new Coordinate(0, 1), r);

        assertEquals(2, rm.getShortestDistance(a, b));
    }

    // ========== Drone 加速测试 ==========

    @Test
    public void testGetShortestDistance_UseDrone() {
        RoadMap rm = new RoadMap();
        FactoryBuilding a = new FactoryBuilding("A", new BuildingType("t", List.of()), List.of());
        FactoryBuilding b = new FactoryBuilding("B", new BuildingType("t", List.of()), List.of());
        a.setLocation(new Coordinate(0, 0));
        b.setLocation(new Coordinate(0, 5));
        rm.addBuilding(a);
        rm.addBuilding(b);
        // 放一架无人机在 (0,0) 附近
        DroneBuilding hub = new DroneBuilding("Hub");
        hub.setLocation(new Coordinate(0, 1));
        rm.addBuilding(hub);
        for (int i = 0; i < 11; ++i) {
            hub.step(i, 0);
            // System.out.println("Step " + i + " IDLE: " + hub.countIdleDrones() + " ACTIVE: " + hub.countActiveDrones() +  " IN: " + hub.countInConstructDrones());
        }

        // System.out.println("IDLE: " + hub.countIdleDrones() + " ACTIVE: " + hub.countActiveDrones() +  " IN: " + hub.countInConstructDrones());
        assertEquals(1, hub.countIdleDrones());
        assertEquals(0, hub.countActiveDrones());
        assertEquals(1, hub.countInConstructDrones());

        // 无路格时 BFS = -1，但无人机可用且距离均 <=20
        int dist = rm.getShortestDistance(a, b);
        // goTime = hub→A (1) + A→B (5) = 6
        assertEquals(6, dist);
        assertEquals(0, hub.countIdleDrones());
        assertEquals(1, hub.countActiveDrones());
        assertEquals(1, hub.countInConstructDrones());

        // 无人机运行总时间：10
        for (int i = 0; i < 11; ++i) {
            hub.step(i + 11, 0);
            // System.out.println("Step "  + (i + 11) + " IDLE: " + hub.countIdleDrones() + " ACTIVE: " + hub.countActiveDrones() +  " IN: " + hub.countInConstructDrones());
        
        }
        assertEquals(2, hub.countIdleDrones());
        assertEquals(0, hub.countActiveDrones());
        assertEquals(1, hub.countInConstructDrones());
    }

    @Test
    public void testGetShortestDistance_DroneTooFarOrNone() {
        RoadMap rm = new RoadMap();
        FactoryBuilding a = new FactoryBuilding("A", new BuildingType("t", List.of()), List.of());
        FactoryBuilding b = new FactoryBuilding("B", new BuildingType("t", List.of()), List.of());
        a.setLocation(new Coordinate(0, 0));
        b.setLocation(new Coordinate(0, 5));
        rm.addBuilding(a);
        rm.addBuilding(b);
        // Drone 在太远处
        DroneBuilding hub = new DroneBuilding("Hub");
        hub.setLocation(new Coordinate(30, 30));
        hub.constructDrone();
        rm.addBuilding(hub);

        // BFS 和无人机都不可用
        assertEquals(-1, rm.getShortestDistance(a, b));
    }

    // ========== 路径和建路测试 ==========

    @Test
    public void testGetOptimalPath_NoExistingRoad() {
        RoadMap rm = new RoadMap();
        FactoryBuilding s = new FactoryBuilding("S", new BuildingType("t", List.of()), List.of());
        FactoryBuilding t = new FactoryBuilding("T", new BuildingType("t", List.of()), List.of());
        s.setLocation(new Coordinate(0, 0));
        t.setLocation(new Coordinate(2, 0));
        rm.addBuilding(s);
        rm.addBuilding(t);
        List<Coordinate> path = rm.getOptimalPath(s, t);
        assertEquals(1, path.size());
        assertEquals(new Coordinate(1, 0), path.getFirst());
    }

    @Test
    public void testCreatePath_AddsRoadsAndDirections() {
        RoadMap rm = new RoadMap();
        MineBuilding m1 = new MineBuilding("M1", "m", new Recipe("m", Map.of(), 1), List.of());
        MineBuilding m2 = new MineBuilding("M2", "m", new Recipe("m", Map.of(), 1), List.of());
        m1.setLocation(new Coordinate(0, 0));
        m2.setLocation(new Coordinate(0, 2));
        rm.addBuilding(m1);
        rm.addBuilding(m2);

        rm.createPath(m1, m2);
        // 检查中间格 (0,1) 被创建为路
        Coordinate mid = new Coordinate(0, 1);
        assertTrue(rm.getRoads().containsKey(mid));
        Road road = rm.getRoads().get(mid);
        // 应该允许从 m1 → mid → m2
        assertTrue(road.getEnterDirections().contains(Direction.NORTH) || road.getEnterDirections().contains(Direction.SOUTH));
//        assertTrue(road.getExitDirections().contains(Direction.NORTH) || road.getExitDirections().contains(Direction.SOUTH));
    }

}
