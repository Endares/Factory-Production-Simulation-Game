package edu.duke.ece651.hw2.simulation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimulationException.
 */
public class SimulationExceptionTest {

    @Test
    public void testExceptionMessage() {
        SimulationException ex = new SimulationException("Error occurred");
        assertEquals("Error occurred", ex.getMessage());
    }
}