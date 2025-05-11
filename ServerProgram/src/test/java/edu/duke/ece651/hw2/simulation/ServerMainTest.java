package edu.duke.ece651.hw2.simulation;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ServerMainTest {
    @Test
    public void testServerStartupAndRequest() throws Exception {
        // 1. 启动服务器线程，相当于执行: java ServerMain src/test/resources/inputs/doors1.json
        Thread serverThread = new Thread(() -> {
            try {
                // 与命令行的“--args="src/test/resources/inputs/doors1.json"”等效
                ServerMain.main(new String[]{"src/test/resources/inputs/doors1.json"});
            } catch (Exception e) {
                e.printStackTrace();
                fail("ServerMain.main() threw an exception: " + e.getMessage());
            }
        });
        serverThread.start();

        // 2. 等待服务器启动（实际项目中可通过日志检测或循环探测端口替代简单sleep）
        Thread.sleep(2000); // 简单粗暴地等 2 秒让服务器完成启动

        // 3. 用 HttpURLConnection 访问 /Simulation 接口，并检查返回 200
        try {
            URL url = new URL("http://localhost:3000/Simulation");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            assertEquals(200, responseCode, "Server /Simulation should return 200 OK");

            // 读取一下响应体（可选）
            // InputStream is = conn.getInputStream();
            // String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            // System.out.println("Response body: " + body);


        } catch (Exception e) {
            fail("Failed to connect to server on port 3000: " + e.getMessage());
        }

        // 4. 测试完毕，尝试中断服务器线程，让测试能够退出
        //    如果你的 ServerMain.main() 里在一个锁上 wait()，可以 Thread.interrupt() 或使用其它方式停止服务器
        serverThread.interrupt();
    }
}
