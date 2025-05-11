package edu.duke.ece651.hw2.simulation;

/**
 * Abstract base class for simulation commands.
 */
public abstract class Command {
    /**
     * Executes the command on the given simulation.
     *
     * @param simulation the simulation to execute the command on.
     * @throws SimulationException if an error occurs during execution.
     */
    public abstract void execute(Simulation simulation) throws SimulationException;
}