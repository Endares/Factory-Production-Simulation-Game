package edu.duke.ece651.hw2.simulation;

public class ComplexRemoveCommand extends Command {
    private String source, destination;

    public ComplexRemoveCommand(String src, String dest) {
        this.source = src;
        this.destination = dest;
    }

    @Override
    public void execute(Simulation simulation) throws SimulationException {
        simulation.complexRemove(source, destination);
    }
}
