package edu.duke.ece651.hw2.simulation;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DummySimulationTest {

    @Test
    public void testInitialValues() {
        DummySimulation sim = new DummySimulation();
        assertEquals(0, sim.getCurrentTimeStep(), "初始 timeStep 应为 0");
        assertEquals(0, sim.getVerbosity(), "初始 verbosity 应为 0");
    }

    @Test
    public void testStep() {
        DummySimulation sim = new DummySimulation();
        sim.step(5);
        assertEquals(5, sim.getCurrentTimeStep(), "调用 step(5) 后，timeStep 应为 5");
        sim.step(3);
        assertEquals(8, sim.getCurrentTimeStep(), "累计调用 step 后，timeStep 应为 8");
    }

    @Test
    public void testSetAndGetVerbosity() {
        DummySimulation sim = new DummySimulation();
        sim.setVerbosity(2);
        assertEquals(2, sim.getVerbosity(), "Verbosity 应设置为 2");
    }

    @Test
    public void testFinish() {
        DummySimulation sim = new DummySimulation();
        // finish() 为空操作，不应抛出异常
        assertDoesNotThrow(sim::finish, "finish() 调用时不应抛异常");
    }

    @Test
    public void testPrintMap() {
        DummySimulation sim = new DummySimulation();
        // printMap() 为空实现，调用时不应抛出异常
        assertDoesNotThrow(sim::printMap, "printMap() 调用时不应抛异常");
    }

    // 如果你为 DummySimulation 增加了 toSerializable() 方法，
    // 以下测试可以验证其输出 JSON 的正确性
    @Test
    public void testToSerializable() {
        if (!hasToSerializable()) {
            return; // 若未扩展该方法，则跳过此测试
        }
        DummySimulation sim = new DummySimulation();
        // 假定扩展后，processCommand 后可修改 timeStep，或者直接通过 step() 来改变状态
        sim.setVerbosity(1);
        sim.step(7);
        JSONObject json = sim.toSerializable();
        assertEquals(7, json.getInt("currentTimeStep"), "JSON 中 currentTimeStep 应为 7");
        assertEquals(1, json.getInt("verbosityLevel"), "JSON 中 verbosityLevel 应为 1");

        // 验证其它必须字段（buildings、recipes、roadMap、mapText）
        assertTrue(json.has("buildings"), "JSON 应包含 buildings 字段");
        assertTrue(json.has("recipes"), "JSON 应包含 recipes 字段");
        assertTrue(json.has("roadMap"), "JSON 应包含 roadMap 字段");
        assertTrue(json.has("mapText"), "JSON 应包含 mapText 字段");

        // 假定 mapText 字段包含 "\n"，测试分行结果
        String mapText = json.getString("mapText");
        String[] lines = mapText.split("\n");
        assertTrue(lines.length >= 1, "mapText 应至少包含一行文本");
    }

    /**
     * 利用反射检查 DummySimulation 是否实现了 toSerializable() 方法
     */
    private boolean hasToSerializable() {
        try {
            DummySimulation.class.getMethod("toSerializable");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}