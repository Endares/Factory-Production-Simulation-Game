package edu.duke.ece651.hw2.simulation;

public class SimpleRemoveCommand extends Command {
    private String source, destination;

    public SimpleRemoveCommand(String src, String dest) {
        this.source = src;
        this.destination = dest;
    }

    @Override
    public void execute(Simulation simulation) throws SimulationException {
        simulation.simpleRemove(source, destination);
    }
}
