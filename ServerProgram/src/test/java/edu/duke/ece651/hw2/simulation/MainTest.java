package edu.duke.ece651.hw2.simulation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Main class.
 */
public class MainTest {
  private ByteArrayOutputStream outContent;
  private ByteArrayOutputStream errContent;

   @BeforeEach
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
    }

  @Test
  void test_main_door1() throws IOException {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(bytes, true);
      
      // Prepare input for the simulation
      String inputCommands = "verbose 2\nrequest 'door' from 'D'\nstep 50\nfinish\n";
      InputStream input = new java.io.ByteArrayInputStream(inputCommands.getBytes());
      
      // Save original streams
      InputStream oldIn = System.in;
      PrintStream oldOut = System.out;
      
      try {
          System.setIn(input);
          System.setOut(out);
          
          // Run the simulation with doors1.json
          String[] args = {"src/test/resources/inputs/doors1.json"};
          Main.main(args);
      } finally {
          System.setIn(oldIn);
          System.setOut(oldOut);
      }
      
      // Get the actual output
      String actual = bytes.toString();
      
      // Print the output to the console for debugging
      System.out.println("=== ACTUAL OUTPUT ===");
      System.out.println(actual);
      System.out.println("=== END OUTPUT ===");
      
      // Write the output to output.txt
      java.nio.file.Path outputPath = java.nio.file.Paths.get("src/test/resources/outputs/outputdoor1.txt");
      java.nio.file.Files.createDirectories(outputPath.getParent());
      java.nio.file.Files.write(outputPath, actual.getBytes());
      
      System.out.println("Output written to: " + outputPath.toAbsolutePath());
  }

  @Test
  void test_main_door2() throws IOException {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(bytes, true);
      
      // Prepare input for the simulation
      String inputCommands = "verbose 2\nrequest 'door' from 'D'\nstep 50\nfinish\n";
      InputStream input = new java.io.ByteArrayInputStream(inputCommands.getBytes());
      
      // Save original streams
      InputStream oldIn = System.in;
      PrintStream oldOut = System.out;
      
      try {
          System.setIn(input);
          System.setOut(out);
          
          // Run the simulation with doors1.json
          String[] args = {"src/test/resources/inputs/doors2.json"};
          Main.main(args);
      } finally {
          System.setIn(oldIn);
          System.setOut(oldOut);
      }
      
      // Get the actual output
      String actual = bytes.toString();
      
      // Print the output to the console for debugging
      System.out.println("=== ACTUAL OUTPUT ===");
      System.out.println(actual);
      System.out.println("=== END OUTPUT ===");
      
      // Write the output to output.txt
      java.nio.file.Path outputPath = java.nio.file.Paths.get("src/test/resources/outputs/outputdoor2.txt");
      java.nio.file.Files.createDirectories(outputPath.getParent());
      java.nio.file.Files.write(outputPath, actual.getBytes());
      
      System.out.println("Output written to: " + outputPath.toAbsolutePath());
  }
    // TODO: problems exits
    // @Test
    // public void testMainWithInvalidArgs() {
    //     // 捕获错误输出
    //     PrintStream originalErr = System.err;
    //     ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    //     System.setErr(new PrintStream(errContent));

    //     Main.main(new String[]{});  // 未传入参数

    //     System.setErr(originalErr);
    //     String output = errContent.toString();
    //     assertTrue(output.contains("Usage: java simulation.Main"));
    // }

    
    // TODO: problems exits
    @Test
    public void testMainWithValidFile() throws IOException {
        // 创建一个临时 JSON 文件，内容有效
        String validJson = """
                {
                  "types": [
                    {"name": "door", "recipes": ["door"]},
                    {"name": "wood", "recipes": ["wood"]}
                  ],
                  "buildings": [
                    {"name": "D", "type": "door", "sources": ["W"]},
                    {"name": "W", "mine": "wood", "sources": []}
                  ],
                  "recipes": [
                    {"output": "door", "ingredients": {"wood": 1}, "latency": 10},
                    {"output": "wood", "ingredients": {}, "latency": 1}
                  ]
                }""";
        Path tempFile = Files.createTempFile("test", ".json");
        Files.write(tempFile, validJson.getBytes());

        // 为了结束交互循环，将 System.in 重定向为包含 finish 命令的输入流
        ByteArrayInputStream inContent = new ByteArrayInputStream("finish\n".getBytes());
        InputStream originalIn = System.in;
        System.setIn(inContent);

        // 捕获输出，确保程序正常运行
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        Main.main(new String[]{tempFile.toAbsolutePath().toString()});

        // 恢复 System.in 和 System.out
        System.setIn(originalIn);
        System.setOut(originalOut);

        // 清理临时文件
        Files.delete(tempFile);

        String output = outContent.toString();
        assertTrue(output.contains("0>"));
        assertTrue(output.contains("Simulation finished"));
    }

    @Test
    public void testMainWithInvalidJson() throws IOException {
        // Create a temporary invalid JSON file.
        String invalidJson = "{ invalid json ";
        Path tempFile = Files.createTempFile("testInvalid", ".json");
        Files.write(tempFile, invalidJson.getBytes());

        // Capture error output.
        PrintStream originalErr = System.err;
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        Main.main(new String[]{tempFile.toAbsolutePath().toString()});

        System.setErr(originalErr);
        Files.delete(tempFile);

        String output = errContent.toString();
        assertTrue(output.contains("Invalid JSON format"));
    }

     /**
     * Test main with a valid JSON file.
     * The JSON contains minimal valid data and interactive loop is ended by "finish" command.
     */
    @Test
    public void testMainValidFile() throws IOException {
        String validJson = """
                {
                  "recipes": [
                    {"output": "door", "ingredients": {"wood": 1}, "latency": 10},
                    {"output": "wood", "ingredients": {}, "latency": 1}
                  ],
                  "types": [
                    {"name": "door", "recipes": ["door"]},
                    {"name": "wood", "recipes": ["wood"]}
                  ],
                  "buildings": [
                    {"name": "D", "type": "door", "sources": ["W"]},
                    {"name": "W", "mine": "wood", "sources": []}
                  ]
                }
                """;
        Path tempFile = Files.createTempFile("valid", ".json");
        Files.writeString(tempFile, validJson);

        // Set System.in to simulate a "finish" command so the interactive loop ends.
        String simulatedInput = "finish\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        // Run main with the temp file path as argument.
        Main.main(new String[]{tempFile.toAbsolutePath().toString()});

        // Restore System.in in @AfterEach will handle cleanup.
        String output = outContent.toString();
        // Expect interactive prompt (e.g., "0>") and finish message.
        assertTrue(output.contains("0>"), "Expected interactive prompt in output.");
        assertTrue(output.toLowerCase().contains("finished"), "Expected simulation finish message.");

        Files.deleteIfExists(tempFile);
    }

    /**
     * Test main with a non-existent file to trigger IOException.
     */
    @Test
    public void testMainIOException() {
        String nonExistentFile = "nonexistent_file.json";
        Main.main(new String[]{nonExistentFile});
        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Error reading file:"), "Expected error message for file reading failure.");
    }


    /**
     * Test main with a JSON that is syntactically valid but missing required fields to trigger SimulationException.
     * In this case, missing the "recipes" field.
     */
    @Test
    public void testMainSimulationException() throws IOException {
        String invalidSimJson = """
                {
                  "types": [
                    {"name": "door", "recipes": ["door"]}
                  ],
                  "buildings": [
                    {"name": "D", "type": "door", "sources": ["W"]}
                  ]
                }
                """;
        Path tempFile = Files.createTempFile("invalidSim", ".json");
        Files.writeString(tempFile, invalidSimJson);

        // Redirect System.in to end interactive loop (if reached, though SimulationException should occur before).
        System.setIn(new ByteArrayInputStream("finish\n".getBytes()));

        Main.main(new String[]{tempFile.toAbsolutePath().toString()});
        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Simulation error:"), "Expected simulation error message for missing recipes field.");

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void test_main_connectAndPrintMap() throws IOException {
        // Capture System.out output.
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true);

        // Prepare input commands for the simulation.
        // Commands: connect building D to W and Hi, step 10, set verbose level to 1, and finish.
        String inputCommands = """
                request 'door' from 'D'
                connect 'D' to 'W'
                connect 'D' to 'Hi'
                printMap
                step 10
                verbose 2
                finish
                """;
        InputStream input = new ByteArrayInputStream(inputCommands.getBytes());

        // Save original standard input and output streams.
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;

        try {
            // Redirect System.in and System.out to our custom streams BEFORE calling Main.main.
            System.setIn(input);
            System.setOut(out);

            // Run the simulation with the specified JSON input file.
            // Adjust the file path if necessary. Note: used "inputs" folder as in the working example.
            String[] args = {"src/test/resources/inputs/phase2_1.json"};
            Main.main(args);
        } finally {
            // Restore the original streams.
            System.setIn(oldIn);
            System.setOut(oldOut);
        }

        // Get the captured simulation output.
        String actual = bytes.toString();

        // Print the captured output to System.out for debugging.
        System.out.println("=== ACTUAL OUTPUT ===");
        System.out.println(actual);
        System.out.println("=== END OUTPUT ===");

        // Optionally, write the output to a file for regression testing.
        // (这部分代码可以根据需要保留或移除)
        Path outputPath = Paths.get("src/test/resources/outputs/outputConnect.txt");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, actual.getBytes());
        System.out.println("Output written to: " + outputPath.toAbsolutePath());
    }

    @Test
    void test_main_phase2_1() throws IOException {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(bytes, true);
      
      // Prepare input for the simulation
      String inputCommands = "verbose 2\nrequest 'door' from 'D'\nstep 50\nfinish\n";
      InputStream input = new java.io.ByteArrayInputStream(inputCommands.getBytes());
      
      // Save original streams
      InputStream oldIn = System.in;
      PrintStream oldOut = System.out;
      
      try {
          System.setIn(input);
          System.setOut(out);
          
          // Run the simulation with doors1.json
          String[] args = {"src/test/resources/inputs/phase2_1.json"};
          Main.main(args);
      } finally {
          System.setIn(oldIn);
          System.setOut(oldOut);
      }
      
      // Get the actual output
      String actual = bytes.toString();
      
      // Print the output to the console for debugging
      System.out.println("=== ACTUAL OUTPUT ===");
      System.out.println(actual);
      System.out.println("=== END OUTPUT ===");
      
      // Write the output to output.txt
      java.nio.file.Path outputPath = java.nio.file.Paths.get("src/test/resources/outputs/output_phase2_1.txt");
      java.nio.file.Files.createDirectories(outputPath.getParent());
      java.nio.file.Files.write(outputPath, actual.getBytes());
      
      System.out.println("Output written to: " + outputPath.toAbsolutePath());
  }

  // @Test
  //   void test_main_phase2_3() throws IOException {
  //     ByteArrayOutputStream bytes = new ByteArrayOutputStream();
  //     PrintStream out = new PrintStream(bytes, true);
      
  //     // Prepare input for the simulation
  //     String inputCommands = "verbose 2\nrequest 'door' from 'D'\nstep 50\nfinish\n";
  //     InputStream input = new java.io.ByteArrayInputStream(inputCommands.getBytes());
      
  //     // Save original streams
  //     InputStream oldIn = System.in;
  //     PrintStream oldOut = System.out;
      
  //     try {
  //         System.setIn(input);
  //         System.setOut(out);
          
  //         // Run the simulation with doors1.json
  //         String[] args = {"src/test/resources/inputs/phase2_3.json"};
  //         Main.main(args);
  //     } finally {
  //         System.setIn(oldIn);
  //         System.setOut(oldOut);
  //     }
      
  //     // Get the actual output
  //     String actual = bytes.toString();
      
  //     // Print the output to the console for debugging
  //     System.out.println("=== ACTUAL OUTPUT ===");
  //     System.out.println(actual);
  //     System.out.println("=== END OUTPUT ===");
      
  //     // Write the output to output.txt
  //     java.nio.file.Path outputPath = java.nio.file.Paths.get("src/test/resources/outputs/output_phase2_3.txt");
  //     java.nio.file.Files.createDirectories(outputPath.getParent());
  //     java.nio.file.Files.write(outputPath, actual.getBytes());
      
  //     System.out.println("Output written to: " + outputPath.toAbsolutePath());
  // }
  

    @Test
    void test_bothRemoval() throws IOException {
      // Capture System.out output.
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(bytes, true);

      String inputCommands = """
                            crm 'D' to 'Hi'
                            crm 'D' to 'M'
                            printMap
                            crm 'D' to 'Ha'
                            crm 'Ha' to 'S1'
                            crm 'D' to 'S1'
                            printMap
                            crm 'W' to 'Ha'
                            crm 'Hi' to 'Ha'
                            printMap
                            srm 'Hi' to 'W'
                            srm 'Ha' to 'Hi'
                            srm 'M' to 'Ha'
                            printMap
                            finish
                            """;
      InputStream input = new ByteArrayInputStream(inputCommands.getBytes());

      // Save original standard input and output streams.
      InputStream oldIn = System.in;
      PrintStream oldOut = System.out;

      try {
          // Redirect System.in and System.out to our custom streams BEFORE calling Main.main.
          System.setIn(input);
          System.setOut(out);

          // Run the simulation with the specified JSON input file.
          // Adjust the file path if necessary. Note: used "inputs" folder as in the working example.
          String[] args = {"src/test/resources/inputs/phase2_3.json"};
          Main.main(args);
      } finally {
          // Restore the original streams.
          System.setIn(oldIn);
          System.setOut(oldOut);
      }

      // Get the captured simulation output.
      String actual = bytes.toString();

      // Print the captured output to System.out for debugging.
      System.out.println("=== ACTUAL OUTPUT ===");
      System.out.println(actual);
      System.out.println("=== END OUTPUT ===");

      // Optionally, write the output to a file for regression testing.
      // (这部分代码可以根据需要保留或移除)
      Path outputPath = Paths.get("src/test/resources/outputs/output_phase2_3.txt");
      Files.createDirectories(outputPath.getParent());
      Files.write(outputPath, actual.getBytes());
      System.out.println("Output written to: " + outputPath.toAbsolutePath());
  }
    
}
