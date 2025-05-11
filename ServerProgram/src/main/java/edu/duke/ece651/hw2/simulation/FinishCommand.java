package edu.duke.ece651.hw2.simulation;

/**
 * Command to finish the simulation.
 */
public class FinishCommand extends Command {
    /**
     * Constructs a FinishCommand.
     */
    public FinishCommand() {}

    @Override
    public void execute(Simulation simulation) throws SimulationException {
        simulation.finish();
        System.out.println("Simulation finished at time " + simulation.getCurrentTimeStep());
    }
}