package edu.duke.ece651.hw2.simulation;

/**
 * Command to advance simulation time.
 */
public class BuildCommand extends Command {
    private int x;
    private int y;
    private String type;

    public BuildCommand(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public String getType(){ return type; }

    @Override
    public void execute(Simulation simulation) throws SimulationException {
        simulation.buildBuilding(type, x, y);
        System.out.println("Built building of type " + type + "on coordinates " + x + ", " + y);
    }
}