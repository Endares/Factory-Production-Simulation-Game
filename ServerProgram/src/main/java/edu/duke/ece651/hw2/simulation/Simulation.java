package edu.duke.ece651.hw2.simulation;

/**
 * Interface for simulation processing.
 */
public interface Simulation {
    /**
     * Processes a command given as a string.
     *
     * @param command the command string.
     */
    void processCommand(String command);

    /**
     * Advances the simulation by the given number of steps.
     *
     * @param steps number of steps to advance (must be >= 1).
     */
    void step(int steps);

    /**
     * Finishes the simulation.
     */
    void finish();

    /**
     * Gets the current simulation time step.
     *
     * @return current time step.
     */
    int getCurrentTimeStep();

    /**
     * Sets the verbosity level of the simulation.
     *
     * @param level the verbosity level.
     */
    void setVerbosity(int level);

    /**
     * Connect two Buildings.
     *
     * @param sourceName the name of source Building
     * @param destName the name of destination Building
     */
    void connectBuildings(String sourceName, String destName) throws SimulationException;

    public void buildBuilding(String typeName, int x, int y) throws SimulationException;

    void removeBuilding(String buildingName) throws SimulationException;

    void printMap();

    void simpleRemove(String src, String dest);

    void complexRemove(String src, String dest);

    void pause();

    void setRate(int rate);

    int getRate();
}