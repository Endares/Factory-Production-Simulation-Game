package edu.duke.ece651.hw2.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a mine building in the simulation.
 */
public class MineBuilding extends BasicBuilding {
    private String mine;
    private Recipe mineRecipe;

    /**
     * Constructs a MineBuilding.
     *
     * @param name    the building name.
     * @param mine    the recipe name produced by the mine.
     * @param sources list of source building names (can be empty or omitted).
     */
    public MineBuilding(String name, String mine, Recipe mineRecipe, List<String> sources) {
        super(name, sources);
        this.mine = mine;
        this.mineRecipe = mineRecipe;
        this.requestQueue = new ArrayList<>();
        this.currentRequest = null;
        this.remainingTime = 0;
        this.storage = new HashMap<>();
    }

    @Override
    public String toString() {
        return super.toString() + "Mine: " + mine + "\n" +
                "MineRecipe: " + mineRecipe.getOutput() + "\n";
    }

    /**
     * Sets the mine recipe reference.
     *
     * @param recipe the Recipe object.
     */
    public void setMineRecipe(Recipe recipe) {
        this.mineRecipe = recipe;
    }

    /**
     * Gets the mine output.
     *
     * @return the mine output recipe name.
     */
    public String getMine() {
        return mine;
    }

    /**
     * Gets the mine's recipe.
     *
     * @return the Recipe.
     */
    public Recipe getMineRecipe() {
        return mineRecipe;
    }

    @Override
    public boolean canBeRemovedImmediately() {
        // 矿山可以立即移除的条件：队列中没有请求且当前没有处理中的请求
        return requestQueue.isEmpty() && currentRequest == null;
    }

    /**
     * For a mine, the provided output is its minefield.
     *
     * @return list containing the mine output.
     */
    @Override
    public List<String> getProvidedOutputs() {
        return Collections.singletonList(mine);
    }

    @Override
    public boolean canProduce(String itemName) {
        return mine.equals(itemName);
    }


    /**
     * Helper method to handle request completion logic
     */
    private void handleCompletedRequest(int currentTimeStep, int verbosity) {
        currentRequest.setStatus(RequestStatus.COMPLETED);
        
        // If this is a user request, we're done
        if (!currentRequest.isUserRequest()) {
            // Deliver the produced item to the requestor
            Building requestor = currentRequest.getRequestor();
            // requestor.deliverItem(mine, 1);
            int transitTime = 0;
            if (this.getLocation() != null && requestor.getLocation() != null) {
                transitTime = simulation.getRoadMap().getShortestDistance(this, requestor);
                if (transitTime < 0) {
                    System.err.println("Warning: No valid path found from " + this.getName() + 
                                    " to " + requestor.getName() + ", using direct delivery");
                    transitTime = 0;
                }
            }
            if (transitTime > 0) {
                simulation.scheduleDelivery(this, requestor, mine, 1, 
                                         currentTimeStep + transitTime);
            } else {
                // if adjacent, deliver immediately
                requestor.deliverItem(mine, 1);
                
                if (verbosity >= 1) {
                    System.out.println("[ingredient delivered]: " + mine + 
                                     " to " + requestor.getName() + 
                                     " from " + name + 
                                     " on cycle " + (currentTimeStep+1));
                    if (requestor instanceof FactoryBuilding factory) {
                        simulation.checkReadyRecipesAtFactory(factory);
                    }
                }
            }
        } else {
            // This is a user request
            System.out.println("[order complete] Order " + currentRequest.getId() + 
                            " completed (" + mine + ") at time " + (currentTimeStep + 1));
        }
    }

    @Override
    public List<Request> step(int currentTimeStep, int verbosity) {
        List<Request> completedRequests = new ArrayList<>();
        boolean processed = false;

        // Process current request if one is in progress
        if (currentRequest != null && remainingTime > 0) {
            remainingTime--;
            if (remainingTime == 0) {
                // Request is completed
                handleCompletedRequest(currentTimeStep, verbosity);
                completedRequests.add(currentRequest);
                currentRequest = null;
                processed = true;
            }
        }

        // Try to select a new request if not currently working on one
        if (currentRequest == null) {
            Request nextRequest = selectNextRequest(currentTimeStep, verbosity);
            if (nextRequest != null) {
                currentRequest = nextRequest;
                remainingTime = mineRecipe.getLatency();
                
                if (!processed) {
                    // Immediately start processing in this time step
                    // Check if completes immediately
                    if (remainingTime == 1) {
                        handleCompletedRequest(currentTimeStep, verbosity);
                        completedRequests.add(currentRequest);
                        currentRequest = null;
                    } else {
                        currentRequest.setStatus(RequestStatus.IN_PROGRESS);
                    }
                } else {
                    currentRequest.setStatus(RequestStatus.IN_PROGRESS);
                }
            }
        }

        return completedRequests;
    }

    @Override
    public Request selectNextRequest(int currentTimeStep, int verbosity) {
        // don't print the message for verbose 2 in output's case
        if (requestQueue.isEmpty()) {
            return null;
        }

        return requestQueue.removeFirst();
    }

    @Override
    public boolean isViable() {
        return true;
    }
}