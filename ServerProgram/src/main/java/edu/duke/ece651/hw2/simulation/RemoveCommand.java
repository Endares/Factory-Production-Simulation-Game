package edu.duke.ece651.hw2.simulation;

public class RemoveCommand extends Command {
    private String buildingName;

    public RemoveCommand(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getName(){ return buildingName; }

    @Override
    public void execute(Simulation simulation) throws SimulationException {
        simulation.removeBuilding(buildingName);
        System.out.println("Removed building named " + buildingName);
    }
}