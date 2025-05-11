package edu.duke.ece651.hw2.simulation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FinishCommand.
 */
public class FinishCommandTest {

    @Test
    public void testExecuteFinishCommand() throws SimulationException {
        DummySimulation simulation = new DummySimulation();
        simulation.step(3);
        FinishCommand cmd = new FinishCommand();
        cmd.execute(simulation);
        // Ensure the simulation time remains as set.
        assertEquals(3, simulation.getCurrentTimeStep());
    }

    @Test
    public void testFinishCommandDoesNotChangeTime() throws SimulationException {
        DummySimulation simulation = new DummySimulation();
        int timeBefore = simulation.getCurrentTimeStep();
        FinishCommand cmd = new FinishCommand();
        cmd.execute(simulation);
        assertEquals(timeBefore, simulation.getCurrentTimeStep());
    }
}