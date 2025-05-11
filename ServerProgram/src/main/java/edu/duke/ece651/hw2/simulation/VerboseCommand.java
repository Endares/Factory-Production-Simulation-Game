package edu.duke.ece651.hw2.simulation;

/**
 * Command to set the verbosity level of the simulation.
 */
public class VerboseCommand extends Command {
    private int level;

    /**
     * Constructs a VerboseCommand.
     *
     * @param level the verbosity level to set.
     */
    public VerboseCommand(int level) {
        this.level = level;
    }

    /**
     * Gets the verbosity level.
     *
     * @return the verbosity level.
     */
    public int getLevel() {
        return level;
    }

    @Override
    public void execute(Simulation simulation) throws SimulationException {
        simulation.setVerbosity(level);
        System.out.println("Verbosity level set to " + level);
    }
}