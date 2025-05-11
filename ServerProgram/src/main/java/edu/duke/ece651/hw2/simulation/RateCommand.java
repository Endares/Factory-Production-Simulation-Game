package edu.duke.ece651.hw2.simulation;

public class RateCommand extends Command {
    private int rate;

    public RateCommand(int rate) { this.rate = rate; }

    @Override
    public void execute(Simulation simulation) throws SimulationException {
        simulation.setRate(rate);
    }
}