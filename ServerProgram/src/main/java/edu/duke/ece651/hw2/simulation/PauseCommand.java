package edu.duke.ece651.hw2.simulation;

public class PauseCommand extends Command {
    @Override
    public void execute(Simulation simulation) throws SimulationException {
        simulation.pause();
        System.out.println("Simulation paused.");
    }
}