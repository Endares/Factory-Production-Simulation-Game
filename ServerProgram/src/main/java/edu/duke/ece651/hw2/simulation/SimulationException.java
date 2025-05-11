package edu.duke.ece651.hw2.simulation;

/**
 * Exception thrown when simulation parsing or validation fails.
 */
public class SimulationException extends Exception {
    /**
     * Constructs a SimulationException with the specified detail message.
     *
     * @param message the detail message.
     */
    public SimulationException(String message) {
        super(message);
    }
}