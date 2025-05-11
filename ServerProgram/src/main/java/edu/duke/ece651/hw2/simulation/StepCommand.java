package edu.duke.ece651.hw2.simulation;

/**
 * Command to advance simulation time.
 */
public class StepCommand extends Command {
    private int steps;

    /**
     * Constructs a StepCommand.
     *
     * @param steps the number of steps to advance.
     */
    public StepCommand(int steps) {
        this.steps = steps;
    }

    /**
     * Gets the number of steps.
     *
     * @return the step count.
     */
    public int getSteps() {
        return steps;
    }

    @Override
    public void execute(Simulation simulation) throws SimulationException {
        simulation.step(steps);
        System.out.println("Advanced simulation by " + steps + " steps. Current time: " + simulation.getCurrentTimeStep());
    }
}