package edu.duke.ece651.hw2.simulation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ClientMain {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:3000";
        String simulationUrl = serverUrl + "/Simulation";
        String instructionUrl = serverUrl + "/Instruction";

        // 1. 初始时，向服务器发起 GET 请求，获取当前模拟状态并以自定义格式美化打印
        try {
            String getResponse = sendGet(simulationUrl);
            JSONObject jsonResponse = new JSONObject(getResponse);
            printFormattedSimulation(jsonResponse);
        } catch (Exception e) {
            System.err.println("GET error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 2. 进入命令行交互模式，每次输入命令后以 POST 方式发送到服务端，并输出美化结果
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Please enter Instruction (`exit` for quit): ");
            String command = scanner.nextLine().trim();
            if (command.isEmpty()) {
                continue;
            }
            if ("exit".equalsIgnoreCase(command)) {
                System.out.println("Exit client");
                break;
            }

            try {
                String postResponse = sendPost(instructionUrl, command);
                JSONObject jsonPostResponse = new JSONObject(postResponse);
                printFormattedSimulation(jsonPostResponse);
            } catch (Exception e) {
                System.err.println("POST error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        scanner.close();
    }

    /**
     * 发起 GET 请求，并返回响应字符串
     */
    private static String sendGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");

        int responseCode = con.getResponseCode();
        InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                ? con.getInputStream() : con.getErrorStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String response = in.lines().collect(Collectors.joining("\n"));
        in.close();
        return response;
    }

    /**
     * 发起 POST 请求，将 payload 内容发送给指定 URL，并返回响应字符串
     */
    private static String sendPost(String urlStr, String payload) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        con.setRequestProperty("Accept", "application/json");

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();
        InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                ? con.getInputStream() : con.getErrorStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String response = in.lines().collect(Collectors.joining("\n"));
        in.close();
        return response;
    }

    /**
     * 解析从服务端返回的 JSON 数据，并以自定义格式输出
     * 不输出 roadMap 字段，mapText 字段中的换行符能正确换行显示。
     */
    private static void printFormattedSimulation(JSONObject json) {
        // 读取基本字段
        int currentTimeStep = json.optInt("currentTimeStep", -1);
        int verbosityLevel = json.optInt("verbosityLevel", -1);
        JSONArray buildingsJson = json.optJSONArray("buildings");
        JSONArray recipesJson = json.optJSONArray("recipes");
        String mapText = json.optString("mapText", "");

        // 将 buildings 和 recipes 输出为逗号分隔的字符串
        String buildings = "";
        if (buildingsJson != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < buildingsJson.length(); i++) {
                sb.append(buildingsJson.getString(i));
                if (i < buildingsJson.length() - 1) {
                    sb.append(", ");
                }
            }
            buildings = sb.toString();
        }

        String recipes = "";
        if (recipesJson != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < recipesJson.length(); i++) {
                sb.append(recipesJson.getString(i));
                if (i < recipesJson.length() - 1) {
                    sb.append(", ");
                }
            }
            recipes = sb.toString();
        }

        // 自定义格式化输出
        String output = "======== Simulation State ========\n" +
                "Current Time Step: " + currentTimeStep + "\n" +
                "Verbosity Level  : " + verbosityLevel + "\n" +
                "Buildings        : " + buildings + "\n" +
                "Recipes          : " + recipes + "\n" +
                "Map Text         :\n" +
                mapText + "\n" + // mapText 中的 \n 会被解释为换行
                "==================================\n";

        System.out.println(output);
    }
}