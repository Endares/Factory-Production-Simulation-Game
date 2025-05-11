package edu.duke.ece651.hw2.simulation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Unit tests for CommandProcessor.
 */
public class CommandProcessorTest {

    private DummySimulation dummySim = new DummySimulation();

    @Test
    public void testParseRequestCommand() throws SimulationException {
        CommandProcessor processor = new CommandProcessor(dummySim);
        Command cmd = processor.parseCommand("request 'door' from 'Factory1'");
        assertInstanceOf(RequestCommand.class, cmd);
        RequestCommand reqCmd = (RequestCommand) cmd;
        assertEquals("door", reqCmd.getItem());
        assertEquals("Factory1", reqCmd.getBuilding());
    }

    @Test
    public void testParseStepCommand() throws SimulationException {
        CommandProcessor processor = new CommandProcessor(dummySim);
        Command cmd = processor.parseCommand("step 10");
        assertInstanceOf(StepCommand.class, cmd);
        StepCommand stepCmd = (StepCommand) cmd;
        assertEquals(10, stepCmd.getSteps());
    }

    @Test
    public void testParseFinishCommand() throws SimulationException {
        CommandProcessor processor = new CommandProcessor(dummySim);
        Command cmd = processor.parseCommand("finish");
        assertInstanceOf(FinishCommand.class, cmd);
    }

    @Test
    public void testParseVerboseCommand() throws SimulationException {
        CommandProcessor processor = new CommandProcessor(dummySim);
        Command cmd = processor.parseCommand("verbose 3");
        assertInstanceOf(VerboseCommand.class, cmd);
        VerboseCommand verboseCmd = (VerboseCommand) cmd;
        assertEquals(3, verboseCmd.getLevel());
    }

    @Test
    public void testParseInvalidCommand() {
        CommandProcessor processor = new CommandProcessor(dummySim);
        Exception exception = assertThrows(SimulationException.class, () -> processor.parseCommand("invalid command"));
        assertEquals("Unknown command", exception.getMessage());
    }

    @Test
    public void testParseEmptyCommand() {
        CommandProcessor processor = new CommandProcessor(dummySim);
        Exception exception = assertThrows(SimulationException.class, () -> processor.parseCommand("   "));
        assertEquals("Empty command", exception.getMessage());
    }

    @Test
    public void testInvalidStepCommandFormat() {
        CommandProcessor processor = new CommandProcessor(dummySim);
        Exception exception = assertThrows(SimulationException.class, () -> processor.parseCommand("step"));
        assertTrue(exception.getMessage().contains("Invalid step command format"));
    }

    @Test
    public void testRequestCommandWithExtraSpaces() throws SimulationException {
        CommandProcessor processor = new CommandProcessor(dummySim);
        Command cmd = processor.parseCommand("   request   'item'   from   'B1'   ");
        assertInstanceOf(RequestCommand.class, cmd);
        RequestCommand reqCmd = (RequestCommand) cmd;
        assertEquals("item", reqCmd.getItem());
        assertEquals("B1", reqCmd.getBuilding());
    }

     @Test
    public void testStepCommandWithZero() {
        CommandProcessor processor = new CommandProcessor(dummySim);
        SimulationException ex = assertThrows(SimulationException.class, () -> 
            processor.parseCommand("step 0")
        );
        assertEquals("Step value must be >= 1", ex.getMessage());
    }

    @Test
    public void testVerboseCommandWithNonNumeric() {
        CommandProcessor processor = new CommandProcessor(dummySim);
        SimulationException ex = assertThrows(SimulationException.class, () -> 
            processor.parseCommand("verbose high")
        );
        assertEquals("Invalid number for verbose command", ex.getMessage());
    }

    @Test
    public void testVerboseCommandWithInvalidFormat() {
        CommandProcessor processor = new CommandProcessor(dummySim);
        SimulationException ex = assertThrows(SimulationException.class, () -> 
            processor.parseCommand("verbose")
        );
        assertEquals("Invalid verbose command format", ex.getMessage());
    }

    @Test
    public void testRequestCommandWithMissingQuotes() {
        CommandProcessor processor = new CommandProcessor(dummySim);
        SimulationException ex = assertThrows(SimulationException.class, () -> 
            processor.parseCommand("request 'item' from ")
        );
        assertEquals("Invalid request command format", ex.getMessage());
    }

    @Test
    public void testProcessCommandErrorOutput() {
        // Test processCommand with an invalid command and capture stderr output.
        CommandProcessor processor = new CommandProcessor(dummySim);
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));
        try {
            processor.processCommand("invalid command");
        } finally {
            System.setErr(originalErr);
        }
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Error: Unknown command"));
    }

    @Test
    public void testParseBuildCommand() throws SimulationException {
        CommandProcessor processor = new CommandProcessor(dummySim);
        Command cmd = processor.parseCommand("build 3 4 MyType");
        assertInstanceOf(BuildCommand.class, cmd);
        BuildCommand buildCmd = (BuildCommand) cmd;
        assertEquals(3, buildCmd.getX());
        assertEquals(4, buildCmd.getY());
        assertEquals("MyType", buildCmd.getType());
    }

    @Test
    public void testParseBuildCommandWithExtraSpaces() throws SimulationException {
        CommandProcessor processor = new CommandProcessor(dummySim);
        Command cmd = processor.parseCommand("  build   10   20   Complex Type Name  ");
        assertInstanceOf(BuildCommand.class, cmd);
        BuildCommand buildCmd = (BuildCommand) cmd;
        assertEquals(10, buildCmd.getX());
        assertEquals(20, buildCmd.getY());
        assertEquals("Complex Type Name", buildCmd.getType());
    }

    @Test
    public void testParseBuildInvalidFormat() {
        CommandProcessor processor = new CommandProcessor(dummySim);
        // 少于 4 段
        SimulationException ex1 = assertThrows(SimulationException.class, () ->
                processor.parseCommand("build 5 6")
        );
        assertEquals("Invalid build command format", ex1.getMessage());

        // 过多空格但仍少参数
        SimulationException ex2 = assertThrows(SimulationException.class, () ->
                processor.parseCommand("build    7")
        );
        assertEquals("Invalid build command format", ex2.getMessage());
    }

    @Test
    public void testParseBuildNonNumericCoordinates() {
        CommandProcessor processor = new CommandProcessor(dummySim);
        SimulationException ex = assertThrows(SimulationException.class, () ->
                processor.parseCommand("build x y TypeName")
        );
        assertEquals("Invalid coordinates for build command", ex.getMessage());
    }
}