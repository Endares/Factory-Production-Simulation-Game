package edu.duke.ece651.hw2.simulation;

import java.util.List;

/**
 * Represents a building in the simulation.
 */
public interface Building {
    public String toString();
    /**
     * Gets the building name.
     *
     * @return the name.
     */
    String getName();

    /**
     * Gets the list of source building names.
     *
     * @return list of sources.
     */
    List<String> getSources();

    /**
     * Add a source building name into Sources.
     *
     * @param source that need to be added.
     */
    void addSource(String source);

    /**
     * Returns the list of outputs this building can provide.
     *
     * @return list of provided output names.
     */
    List<String> getProvidedOutputs();
    
    /**
     * Checks if this building can produce the specified item.
     *
     * @param itemName the item name to check.
     * @return true if this building can produce the item, false otherwise.
     */
    boolean canProduce(String itemName);

    /**
     * Adds a request to this building's queue.
     *
     * @param request the request to add.
     */
    void addRequest(Request request);

    /**
     * Returns the queue length for this building.
     *
     * @return the number of requests in queue.
     */
    int getQueueLength();

    /**
     * Delivers a produced item to this building.
     *
     * @param item     the item name.
     * @param quantity the quantity of the item.
     */
    void deliverItem(String item, int quantity);

    /**
     * Processes the next time step for this building.
     *
     * @param currentTimeStep the current time step in the simulation.
     * @param verbosity       the verbosity level.
     * @return list of completed requests during this time step.
     */
    List<Request> step(int currentTimeStep, int verbosity);

    /**
     * Selects the next request to work on based on the current policy.
     *
     * @param currentTimeStep the current time step.
     * @param verbosity       the verbosity level.
     * @return the selected request or null if none can be selected.
     */
    Request selectNextRequest(int currentTimeStep, int verbosity);

    /**
     * Processes the ingredients for the specified item.
     *
     * @param item    the item name
     */
    void processIngredients(String item);

    /**
     * Gets the location of this building.
     *
     * @return the coordinate of the building.
     */
    Coordinate getLocation();

    /**
     * Sets the location of this building.
     *
     * @param location the new coordinate of the building.
     */
    void setLocation(Coordinate location);


    /**
     * Gets the simulation for this building.
     *
     * @return the BasicSimulation object.
     */
    BasicSimulation getSimulation();

    /**
     * Sets the simulation for this building.
     *
     * @param simulation the BasicSimulation object.
     */
    void setSimulation(BasicSimulation simulation);

    /*
     * Check if the building is viable.
     */
    boolean isViable();

    /**
     * Checks if the building can be removed immediately.
     *
     * @return true if the building can be removed immediately, false otherwise.
     */
    boolean canBeRemovedImmediately();

    /**
     * Marks the building for removal.
     */
    void markForRemoval();

    /**
     * Checks if the building is marked for removal.
     * @return true if the building is marked for removal, false otherwise.
     */
    boolean isMarkedForRemoval();
}