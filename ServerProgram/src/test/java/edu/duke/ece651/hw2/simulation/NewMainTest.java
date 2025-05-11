package edu.duke.ece651.hw2.simulation;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

class NewMainTest {
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(originalIn);
    }

    @Test
    void testMain_FileNotFound_PrintsError() {
        String[] args = {"nonexistent.json"};
        // doesn't call exit, just prints error
        NewMain.main(args);
        String err = errContent.toString();
        assertTrue(err.contains("Error reading file"));
    }

    @Test
    void testMain_MissingRecipes_PrintsSimulationError() throws IOException {
        // JSON missing "recipes"
        Path tmp = Files.createTempFile("missing", ".json");
        String json = """
            { "types": [], "buildings": [] }
        """;
        Files.writeString(tmp, json);
        NewMain.main(new String[]{tmp.toString()});
        String err = errContent.toString();
        assertTrue(err.contains("Simulation error: Missing 'recipes' field"));
    }

    @Test
    void testMain_MissingTypes_PrintsSimulationError() throws IOException {
        // Has recipes but no types
        Path tmp = Files.createTempFile("missingTypes", ".json");
        String json = """
            { "recipes": [], "buildings": [] }
        """;
        Files.writeString(tmp, json);
        NewMain.main(new String[]{tmp.toString()});
        String err = errContent.toString();
        assertTrue(err.contains("Simulation error"));
    }

    @Test
    void testMain_ValidMinimalJson_RunInteractiveFinish() throws Exception {
        // Build a minimal valid configuration
        Path tmp = Files.createTempFile("valid", ".json");
        String json = """
        {
          "recipes": [
            { "output":"wood", "ingredients":{}, "latency":1 }
          ],
          "types": [
            { "name":"T1","type":"factory","info":{ "recipes":["wood"] } }
          ],
          "buildings": [
            { "name":"B1","type":"factory","x":0,"y":0,"sources":[] }
          ],
          "connections": []
        }
        """;
        Files.writeString(tmp, json);
        // supply "finish" so runInteractive exits immediately
        System.setIn(new ByteArrayInputStream("finish\n".getBytes()));
        NewMain.main(new String[]{tmp.toString()});
        String out = outContent.toString();
        // Should see initial prompt "0>"
        assertTrue(out.contains("0>"), "\n[Output]: " + out + "\n");
    }

    @Test
    void testExtractFactoryTypes_MixedBuildableTypes() throws JSONException {
        // 手动构造 BuildableType map，然后调用 extractFactoryTypes
        Map<String, BuildableType> bts = Map.of(
                "S1", new BuildableType("S1","storage",new JSONObject("{\"stores\":\"bolt\",\"capacity\":10,\"priority\":1.0}")),
                "F1", new BuildableType("F1","factory",new JSONObject("{\"recipes\":[\"r1\",\"r2\"]}")),
                "M1", new BuildableType("M1","mine",new JSONObject("{\"mine\":\"metal\"}")),
                "D1", new BuildableType("D1","drone",new JSONObject("{}"))
        );
        var res = NewMain.extractBuildingTypes(bts);
        assertEquals(1, res.size());
        assertTrue(res.containsKey("F1"));
        BuildingType bt = res.get("F1");
        assertEquals("F1", bt.getName());
        assertEquals(2, bt.getRecipes().size());
        assertEquals(List.of("r1","r2"), bt.getRecipes());
    }
}