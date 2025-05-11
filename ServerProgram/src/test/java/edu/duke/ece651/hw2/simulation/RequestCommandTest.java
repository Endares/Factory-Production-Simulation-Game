package edu.duke.ece651.hw2.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Test cases for RequestCommand.
 */
public class RequestCommandTest {

    /**
     * 用于测试 FactoryBuilding 的子类，用以记录 processIngredients 调用情况。
     */
    public static class TestFactoryBuilding extends FactoryBuilding {
        public boolean processIngredientsCalled = false;
        public String processIngredientsItem = null;

        public TestFactoryBuilding(String name, BuildingType buildingType, List<String> sources) {
            super(name, buildingType, sources);
        }

        @Override
        public void processIngredients(String item) {
            processIngredientsCalled = true;
            processIngredientsItem = item;
            // 调用真实逻辑（可选），这里不再递归调用实际逻辑
        }
    }

    /**
     * 辅助方法，用于构造 BasicSimulation 实例。
     */
    private BasicSimulation createSimulation(Map<String, Building> buildings,
                                             Map<String, Recipe> recipes,
                                             Map<String, BuildingType> buildingTypes) {
        return new BasicSimulation(buildings, recipes, buildingTypes);
    }

    /**
     * 测试 RequestCommand 的 getter 方法。
     */
    @Test
    public void testGetters() {
        RequestCommand rc = new RequestCommand("itemA", "BuildingA");
        assertEquals("itemA", rc.getItem());
        assertEquals("BuildingA", rc.getBuilding());
    }

    /**
     * 测试当传入的 Simulation 不是 BasicSimulation 时，应抛出异常。
     */
    @Test
    public void testExecuteUnsupportedSimulation() {
        RequestCommand rc = new RequestCommand("itemA", "BuildingA");
        // 使用 DummySimulation（已存在于项目中）来模拟不支持的 Simulation 类型
        DummySimulation dummySim = new DummySimulation();
        SimulationException ex = assertThrows(SimulationException.class, () -> rc.execute(dummySim));
        assertEquals("Unsupported simulation type for RequestCommand", ex.getMessage());
    }

    /**
     * 测试当 simulation 中不包含指定建筑时，应抛出异常。
     */
    @Test
    public void testExecuteNoBuilding() {
        // 建筑 map 为空，但配方 map 包含 itemA
        Map<String, Building> buildings = new HashMap<>();
        Map<String, Recipe> recipes = new HashMap<>();
        recipes.put("itemA", new Recipe("itemA", Map.of("ing1", 1), 5));
        Map<String, BuildingType> types = new HashMap<>();

        BasicSimulation sim = createSimulation(buildings, recipes, types);
        RequestCommand rc = new RequestCommand("itemA", "BuildingA");
        SimulationException ex = assertThrows(SimulationException.class, () -> rc.execute(sim));
        assertEquals("Building 'BuildingA' does not exist", ex.getMessage());
    }

    /**
     * 测试当 simulation 中不包含指定配方时，应抛出异常。
     */
    @Test
    public void testExecuteNoRecipe() {
        // 使用 MineBuilding 进行测试
        Map<String, Building> buildings = new HashMap<>();
        // MineBuilding 的 mine 字段为 "itemA"
        Recipe recipeA = new Recipe("itemA", Map.of("ing1", 1), 5);
        MineBuilding mine = new MineBuilding("BuildingA", "itemA", recipeA , new ArrayList<>());
        buildings.put("BuildingA", mine);

        // recipes map 中不包含 itemA
        Map<String, Recipe> recipes = new HashMap<>();
        Map<String, BuildingType> types = new HashMap<>();

        BasicSimulation sim = createSimulation(buildings, recipes, types);
        RequestCommand rc = new RequestCommand("itemA", "BuildingA");
        SimulationException ex = assertThrows(SimulationException.class, () -> rc.execute(sim));
        assertEquals("Recipe 'itemA' does not exist", ex.getMessage());
    }

    /**
     * 测试当建筑不能生产指定产品时，应抛出异常。
     * 此处使用 MineBuilding，当 mine 字段不等于请求的 item 时，即不可生产。
     */
    @Test
    public void testExecuteBuildingCannotProduce() {
        Map<String, Building> buildings = new HashMap<>();
        Recipe recipeA = new Recipe("itemA", Map.of("ing1", 1), 5);
        // MineBuilding 的 mine 字段为 "iron"，与请求的 "itemA" 不符
        MineBuilding mine = new MineBuilding("BuildingA", "iron", recipeA ,new ArrayList<>());
        buildings.put("BuildingA", mine);

        Map<String, Recipe> recipes = new HashMap<>();
        recipes.put("itemA", new Recipe("itemA", Map.of("ing1", 1), 5));
        Map<String, BuildingType> types = new HashMap<>();

        BasicSimulation sim = createSimulation(buildings, recipes, types);
        RequestCommand rc = new RequestCommand("itemA", "BuildingA");
        SimulationException ex = assertThrows(SimulationException.class, () -> rc.execute(sim));
        assertEquals("Building 'BuildingA' cannot produce 'itemA'", ex.getMessage());
    }

    /**
     * 测试在 FactoryBuilding 情况下成功执行 RequestCommand，
     * 并验证 processIngredients 被调用。
     */
    @Test
    public void testExecuteSuccessFactoryBuilding() throws SimulationException {
        // 构造包含 itemA 的 BuildingType
        List<String> typeRecipes = new ArrayList<>();
        typeRecipes.add("itemA");
        BuildingType bt = new BuildingType("FactoryType", typeRecipes);

        // 使用 TestFactoryBuilding 来验证 processIngredients 调用情况
        List<String> sources = new ArrayList<>(); // 可为空
        TestFactoryBuilding factory = new TestFactoryBuilding("BuildingA", bt, sources);
        Map<String, Building> buildings = new HashMap<>();
        buildings.put("BuildingA", factory);

        // 配置 recipes 包含 itemA，且 ingredients 非空（以便触发 processIngredients 内部逻辑）
        Map<String, Recipe> recipes = new HashMap<>();
        Recipe recipeA = new Recipe("itemA", Map.of("ing1", 1), 5);
        recipes.put("itemA", recipeA);

        // buildingTypes 包含该类型
        Map<String, BuildingType> types = new HashMap<>();
        types.put("FactoryType", bt);

        BasicSimulation sim = createSimulation(buildings, recipes, types);
        int initialNextRequestId = sim.gettheNextRequestId();

        RequestCommand rc = new RequestCommand("itemA", "BuildingA");
        rc.execute(sim);

        // 检查 FactoryBuilding 的请求队列中增加了请求
        assertEquals(1, factory.getQueueLength());
        // 检查 request 的属性
        Request req = factory.requestQueue.getFirst();  // 直接访问 protected 字段
        assertEquals(recipeA, req.getRecipe());
        assertTrue(req.isUserRequest());
        assertEquals(initialNextRequestId, req.getId());
        // 检查 processIngredients 被调用，并传入 "itemA"
        assertTrue(factory.processIngredientsCalled);
        assertEquals("itemA", factory.processIngredientsItem);
    }

    /**
     * 测试在 MineBuilding 情况下成功执行 RequestCommand，
     * 对于 MineBuilding，不会调用 processIngredients。
     */
    @Test
    public void testExecuteSuccessMineBuilding() throws SimulationException {
        // 创建一个 MineBuilding，其 mine 为 "itemA"
        Map<String, Building> buildings = new HashMap<>();
        Map<String, Recipe> recipes = new HashMap<>();
        Recipe recipeA = new Recipe("itemA", Map.of(), 5);
        recipes.put("itemA", recipeA);
        MineBuilding mine = new MineBuilding("BuildingB", "itemA", recipeA, new ArrayList<>());
        buildings.put("BuildingB", mine);

        

        // 对于 MineBuilding，不涉及 buildingTypes
        Map<String, BuildingType> types = new HashMap<>();

        BasicSimulation sim = createSimulation(buildings, recipes, types);
        int initialNextRequestId = sim.gettheNextRequestId();

        RequestCommand rc = new RequestCommand("itemA", "BuildingB");
        rc.execute(sim);

        // 检查 MineBuilding 的请求队列中增加了请求
        assertEquals(1, mine.getQueueLength());
        // 检查请求的属性
        // 注意：MineBuilding 内部独有的 requestQueue 与 BasicBuilding.requestQueue 可能存在影子问题，
        // 这里通过 getQueueLength() 判断请求数
        // 同时无法检测 processIngredients（因为 MineBuilding 不做该调用）
        Request req = mine.requestQueue.getFirst(); // 直接访问 MineBuilding 中的私有字段（在测试中可设为包内可访问）
        assertEquals(recipeA, req.getRecipe());
        assertTrue(req.isUserRequest());
        assertEquals(initialNextRequestId, req.getId());
    }
}