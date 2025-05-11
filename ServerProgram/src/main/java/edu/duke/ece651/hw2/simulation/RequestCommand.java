package edu.duke.ece651.hw2.simulation;

/**
 * Command to request an item from a building.
 */
public class RequestCommand extends Command {
    private String item;
    private String building;

    /**
     * Constructs a RequestCommand.
     *
     * @param item     the requested recipe name.
     * @param building the building from which to request.
     */
    public RequestCommand(String item, String building) {
        this.item = item;
        this.building = building;
    }

    /**
     * Gets the requested item.
     *
     * @return the recipe name.
     */
    public String getItem() {
        return item;
    }

    /**
     * Gets the building name.
     *
     * @return the building name.
     */
    public String getBuilding() {
        return building;
    }

    @Override
    public void execute(Simulation simulation) throws SimulationException {
        // Cast simulation to BasicSimulation to access request processing.
        if (simulation instanceof BasicSimulation sim) {
            // THIS IS WHERE calls processIngredients
            sim.handleRequestCommand(item, building);
        } else {
            throw new SimulationException("Unsupported simulation type for RequestCommand");
        }
    }
}