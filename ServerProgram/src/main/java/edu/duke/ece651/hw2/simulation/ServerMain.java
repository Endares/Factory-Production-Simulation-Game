package edu.duke.ece651.hw2.simulation;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONException;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class ServerMain {
    /**
     * Entry point for the simulation server.
     *
     * @param args Command line arguments; the first argument should be the JSON file path.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java simulation.ServerMain <json-file>");
            System.exit(0);
        }
        String filePath = args[0];
        SimulationParser parser = new SimulationParser();
        try {
            // 解析 JSON 文件以及相关组件
            JsonNode json = parser.parseJsonFile(filePath);
            Map<String, Recipe> recipes = parser.parseRecipes(json);
            Map<String, BuildingType> buildingTypes = parser.parseTypes(json, recipes);
            Map<String, Building> buildings = parser.parseBuildings(json, buildingTypes, recipes);
            parser.validateInput(buildings, recipes);
            BasicSimulation simulation = BasicSimulation.createSimulation(buildings, recipes, buildingTypes);
            parser.parseConnections(json, simulation);

            boolean realTime = false;
            for (String arg : args) {
                if ("real-time".equalsIgnoreCase(arg)) {
                    realTime = true;
                    break;
                }
            }
            startServer(simulation, realTime);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        } catch (JSONException e) {
            System.err.println("Invalid JSON format: " + e.getMessage());
            System.exit(1);
        } catch (SimulationException e) {
            System.err.println("Simulation error: " + e.getMessage());
            System.exit(1);
        }
    }

    static void startServer(BasicSimulation simulation, final boolean realTime) {
        if (realTime) {
            System.out.println("===== REAL TIME MODE =====");
        }

        try {
            simulation.printMap();
            // 创建并启动 HTTP 服务器，监听 3000 端口
            HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);

            // 定义 GET 接口：访问 /Simulation 时返回当前模拟状态 JSON
            server.createContext("/Simulation", exchange -> {
                // 处理 OPTIONS 请求，返回 204 No Content
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    addCorsHeaders(exchange);
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String jsonResponse = simulation.toSerializable().toString();
                    addCorsHeaders(exchange);
                    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                    byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                } else {
                    addCorsHeaders(exchange);
                    exchange.sendResponseHeaders(405, -1);  // 返回 405 方法不允许
                }
            });

            // 定义 POST 接口：处理其它指令，更新 simulation 状态后返回更新后的 JSON
            server.createContext("/Instruction", exchange -> {
                // 同样先处理 OPTIONS 请求
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    addCorsHeaders(exchange);
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    addCorsHeaders(exchange);
                    InputStream is = exchange.getRequestBody();
                    String requestBody = new BufferedReader(new InputStreamReader(is))
                            .lines().collect(Collectors.joining("\n"));
                    is.close();

                    // 用 processCommand() 处理命令并更新 simulation 状态
                    System.out.println("Received " + exchange.getRequestMethod() +
                            " request for " + exchange.getRequestURI());
                    System.out.println("Request body: " + requestBody);
                    synchronized (simulation) {
                        System.out.println("Processing command: " + requestBody);
                        simulation.processCommand(requestBody);
                    }
                    String jsonResponse = simulation.toSerializable().toString();
                    // System.out.println("jsonResponse: " + jsonResponse);
                    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                    byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseBytes);
                    os.close();
                } else {
                    addCorsHeaders(exchange);
                    exchange.sendResponseHeaders(405, -1);  // 返回 405 方法不允许
                }
            });

            // 使用默认 Executor，并启动服务器
            server.setExecutor(null);
            server.start();
            System.out.println("Server started on port 3000.");

            // 新线程：每 1 秒，获得 simulation 锁后调用 getRate() 次 step() 方法，并存储当前状态到本地文件
            new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(1000);
                        synchronized (simulation) {
                            if (realTime) {
                                simulation.step(simulation.getRate());
                            }
                            // 将 simulation 当前状态转换成 JSON 字符串（格式化输出便于查看）
                            String jsonState = simulation.toSerializable().toString(4);
                            // 存储到本地文件 "simulation_state.json"
                            try (FileOutputStream fos = new FileOutputStream("simulation_state.json");
                                 OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                                writer.write(jsonState);
                            } catch (IOException e) {
                                System.err.println("Error writing simulation state: " + e.getMessage());
                            }
                        }
                    } catch (InterruptedException e) {
                        System.err.println("Stepping thread interrupted: " + e.getMessage());
                        break;
                    }
                }
            }).start();

            // 阻塞主线程保持服务器一直运行
            Object lock = new Object();
            synchronized (lock) {
                lock.wait();
            }
        } catch (IOException e) {
            System.err.println("Failed to create server: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("Server interrupted: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * 添加 CORS 支持的响应头，允许所有来源和 GET、POST、OPTIONS 方法。
     */
    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
}