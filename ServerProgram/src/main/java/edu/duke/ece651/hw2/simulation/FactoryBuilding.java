package edu.duke.ece651.hw2.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a factory building in the simulation.
 */
public class FactoryBuilding extends BasicBuilding{
    private BuildingType buildingType;

    /**
     * Constructs a FactoryBuilding.
     *
     * @param name         the building name.
     * @param buildingType the BuildingType object defined in types.
     * @param sources      list of source building names.
     */
    
    public FactoryBuilding(String name, BuildingType buildingType, List<String> sources) {
        super(name, sources);
        this.buildingType = buildingType;
    }

    @Override
    public String toString() {
        return super.toString() + "BuildingType: " + buildingType.getName() + "\n" +
                "Type Recipes: " + buildingType.getRecipes() + "\n";
    }

    /**
     * Gets the BuildingType associated with this factory.
     *
     * @return the BuildingType.
     */
    public BuildingType getBuildingType() {
        return buildingType;
    }

    /**
     * For a factory, the provided outputs are the recipes defined in its building type.
     *
     * @return list of provided output names.
     */
    @Override
    public List<String> getProvidedOutputs() {
        return new ArrayList<>(buildingType.getRecipes());
    }

    @Override
    public boolean canProduce(String itemName) {
        return buildingType.getRecipes().contains(itemName);
    }

    @Override
    public boolean canBeRemovedImmediately() {
        // 工厂可以立即移除的条件：队列中没有请求且当前没有处理中的请求
        return requestQueue.isEmpty() && currentRequest == null;
    }

    /**
     * adding the request for the factory building
     * 
     * @param request the Request object.
     */
    @Override
    public void addRequest(Request request) {
        requestQueue.add(request);

    }

    /**
     * Helper method to handle request completion logic
     */
    private void handleCompletedRequest(int currentTimeStep, int verbosity) {
        currentRequest.setStatus(RequestStatus.COMPLETED);
        
        // If this is a user request, we're done
        if (!currentRequest.isUserRequest()) {
            // Deliver the produced item to the requestor
            Recipe recipe = currentRequest.getRecipe();
            Building requestor = currentRequest.getRequestor();
            // requestor.deliverItem(recipe.getOutput(), 1);
            // use scheduled delivery
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
                simulation.scheduleDelivery(this, requestor, recipe.getOutput(), 1, 
                                         currentTimeStep + transitTime);
            } else {
                // if adjacent, deliver immediately
                requestor.deliverItem(recipe.getOutput(), 1);
                
                if (verbosity >= 1) {
                    System.out.println("[ingredient delivered]: " + recipe.getOutput() + 
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
                            " completed (" + currentRequest.getRecipe().getOutput() + 
                            ") at time " + (currentTimeStep+1));
        }
    }
        

    @Override
    public List<Request> step(int currentTimeStep, int verbosity) {
        List<Request> completedRequests = new ArrayList<>();
        boolean processed = false;  
        // 1. Process current request if one is in progress
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

        // 2. Try to select a new request if not currently working on one
        if (currentRequest == null) {
            Request nextRequest = selectNextRequest(currentTimeStep, verbosity);
            if (nextRequest != null) {
                currentRequest = nextRequest;
                remainingTime = currentRequest.getRecipe().getLatency();
                if (processed) {
                    return completedRequests;
                }
                
                // Immediately start processing in this time step
                // Check if the request completes immediately (for latency=1 cases)
                if (remainingTime == 1) {
                    handleCompletedRequest(currentTimeStep, verbosity);
                    completedRequests.add(currentRequest);
                    currentRequest = null;
                } else {
                    // Request not completed yet, set status to IN_PROGRESS
                    currentRequest.setStatus(RequestStatus.IN_PROGRESS);

                    // Consume the ingredients for the recipe
                    Recipe recipe = currentRequest.getRecipe();
                    for (Map.Entry<String, Integer> entry : recipe.getIngredients().entrySet()) {
                        String ingredient = entry.getKey();
                        int required = entry.getValue();
                        int currentAmount = storage.getOrDefault(ingredient, 0);
                        storage.put(ingredient, currentAmount - required);
                    }
                }
            }
        }

        return completedRequests;
    }

    @Override
    public Request selectNextRequest(int currentTimeStep, int verbosity) {
        // Default implementation: FIFO policy
        if (verbosity >= 2 && !requestQueue.isEmpty()) {
            System.out.println("[recipe selection]: " + name + " has fifo on cycle " + (currentTimeStep + 1));
        }

        // First pass: print status of all requests
        if (verbosity >= 2) {
            for (int i = 0; i < requestQueue.size(); i++) {
                Request request = requestQueue.get(i);
                Recipe recipe = request.getRecipe();
                boolean isReady = true;
                List<String> missingIngredients = new ArrayList<>();

                // Check if all ingredients are available
                for (Map.Entry<String, Integer> entry : recipe.getIngredients().entrySet()) {
                    String ingredient = entry.getKey();
                    int required = entry.getValue();
                    int available = storage.getOrDefault(ingredient, 0);

                    if (available < required) {
                        isReady = false;
                        // Track missing ingredients for verbosity reporting
                        int missing = required - available;
                        for (int j = 0; j < missing; j++) {
                            missingIngredients.add(ingredient);
                        }
                    }
                }

                // Report status
                if (isReady) {
                    System.out.println("    " + i + ": is ready");
                } else {
                    // Format missing ingredients list
                    Map<String, Integer> missing = new HashMap<>();
                    for (String ingredient : missingIngredients) {
                        missing.put(ingredient, missing.getOrDefault(ingredient, 0) + 1);
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("    ").append(i).append(": is not ready, waiting on {");
                    boolean first = true;
                    for (Map.Entry<String, Integer> entry : missing.entrySet()) {
                        if (!first) {
                            sb.append(", ");
                        }
                        first = false;
                        if (entry.getValue() > 1) {
                            sb.append(entry.getValue()).append("x ");
                        }
                        sb.append(entry.getKey());
                    }
                    sb.append("}");
                    System.out.println(sb);
                }
            }
        }

        // Second pass: find first ready request
        for (int i = 0; i < requestQueue.size(); i++) {
            Request request = requestQueue.get(i);
            Recipe recipe = request.getRecipe();
            boolean isReady = true;

            // Check if all ingredients are available
            for (Map.Entry<String, Integer> entry : recipe.getIngredients().entrySet()) {
                String ingredient = entry.getKey();
                int required = entry.getValue();
                int available = storage.getOrDefault(ingredient, 0);

                if (available < required) {
                    isReady = false;
                    break;
                }
            }

            // If the request is ready, select it
            if (isReady) {
                if (verbosity >= 2) {
                    System.out.println("    Selecting " + i);
                }
                // Remove from queue and return
                return requestQueue.remove(i);
            }
        }

        return null;  // No ready requests
    }

    @Override
    public boolean isViable() {
        // 工厂需要能够获取所有可能需要的原料
        for (String recipeName : buildingType.getRecipes()) {
            Recipe recipe = simulation.getRecipes().get(recipeName);
            
            // 检查每种原料是否可获取
            for (String ingredient : recipe.getIngredients().keySet()) {
                boolean canGet = false;
                
                for (String sourceName : sources) {
                    Building source = simulation.getBuildings().get(sourceName);
                    if (source != null && source.canProduce(ingredient)) {
                        // 还需要检查是否有有效的路径连接
                        if (simulation.getRoadMap().getShortestDistance(source, this) >= 0) {
                            canGet = true;
                            break;
                        }
                    }
                }
                
                if (!canGet) {
                    return false; // 找不到提供该原料的来源
                }
            }
        }
        
        return true; // 所有原料都可以获取
    }

}