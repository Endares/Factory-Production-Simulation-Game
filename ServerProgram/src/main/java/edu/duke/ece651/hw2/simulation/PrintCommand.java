package edu.duke.ece651.hw2.simulation;

public class PrintCommand extends Command {
    @Override
    public void execute(Simulation simulation) throws SimulationException {
        simulation.printMap();
    }
}
