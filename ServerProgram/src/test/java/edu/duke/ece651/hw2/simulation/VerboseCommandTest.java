package edu.duke.ece651.hw2.simulation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VerboseCommand.
 */
public class VerboseCommandTest {

    @Test
    public void testExecuteVerboseCommand() throws SimulationException {
        DummySimulation simulation = new DummySimulation();
        VerboseCommand cmd = new VerboseCommand(2);
        cmd.execute(simulation);
        assertEquals(2, simulation.getVerbosity());
    }

    @Test
    public void testChangeVerbosityMultipleTimes() throws SimulationException {
        DummySimulation simulation = new DummySimulation();
        VerboseCommand cmd1 = new VerboseCommand(1);
        VerboseCommand cmd2 = new VerboseCommand(5);
        cmd1.execute(simulation);
        assertEquals(1, simulation.getVerbosity());
        cmd2.execute(simulation);
        assertEquals(5, simulation.getVerbosity());
    }
}