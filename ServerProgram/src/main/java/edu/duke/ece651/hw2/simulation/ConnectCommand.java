package edu.duke.ece651.hw2.simulation;

public class ConnectCommand extends Command {
    private String sourceName;
    private String destName;

    public ConnectCommand(String sourceName, String destName) {
        this.sourceName = sourceName;
        this.destName = destName;
    }

    @Override
    public void execute(Simulation simulation) throws SimulationException {
        simulation.connectBuildings(sourceName, destName);
        System.out.println("Connected " + sourceName + " to " + destName);
    }
}