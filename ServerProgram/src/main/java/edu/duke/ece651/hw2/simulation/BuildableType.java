package edu.duke.ece651.hw2.simulation;

import org.json.JSONObject;

public class BuildableType {
    private String name;       // 类型名称，如"Bolt Storage (100)"
    private String type;       // 建筑种类："factory", "storage", "mine"
    private JSONObject info;   // 类型特定信息，格式根据type不同而不同

    public BuildableType(String name, String type, JSONObject info) {
        this.name = name;
        this.type = type;
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public JSONObject getInfo() {
        return info;
    }
}