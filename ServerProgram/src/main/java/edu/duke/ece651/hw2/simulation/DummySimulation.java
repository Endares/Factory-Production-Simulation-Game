package edu.duke.ece651.hw2.simulation;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A dummy simulation implementation for unit testing command executions.
 */
public class DummySimulation implements Simulation {
    private int timeStep = 0;
    private int verbosity = 0;
    private int rate = 2; // 定时步进时，每秒步进次数

    @Override
    public void processCommand(String command) {
        // 模拟处理命令：每收到一次命令，timeStep 增加 10
        timeStep += 10;
    }

    @Override
    public void step(int steps) {
        timeStep += steps;
    }

    // 便利方法，等价于 step(1)
    public void step() {
        step(1);
    }

    @Override
    public void finish() {
        // 不需要在 dummy 实现中做什么
    }

    @Override
    public int getCurrentTimeStep() {
        return timeStep;
    }

    @Override
    public void setVerbosity(int level) {
        verbosity = level;
    }

    public int getVerbosity() {
        return verbosity;
    }

    @Override
    public void connectBuildings(String sourceName, String destName) throws SimulationException {

    }

    @Override
    public void printMap() {
        System.out.println("Dummy simulation map");
    }

    @Override
    public void pause() {
        rate = 0;
    }

    // 新增方法：返回步进速率
    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    // 新增方法：将当前状态转换为 JSON 对象
    public JSONObject toSerializable() {
        JSONObject result = new JSONObject();
        result.put("currentTimeStep", timeStep);
        result.put("verbosityLevel", verbosity);
        result.put("buildings", new JSONArray());
        result.put("recipes", new JSONArray());
        result.put("roadMap", new JSONArray()); // 测试时不关心该字段
        result.put("mapText", "Dummy Map\nLine2");
        return result;
    }

    @Override
    public void simpleRemove(String src, String dest) {}

    @Override
    public void complexRemove(String src, String dest) {}

    @Override
    public void buildBuilding(String typeName, int x, int y) throws SimulationException {}

    @Override
    public void removeBuilding(String name) throws SimulationException {}
}