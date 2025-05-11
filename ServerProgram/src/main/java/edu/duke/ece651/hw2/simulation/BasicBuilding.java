package edu.duke.ece651.hw2.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of the Building interface with common functionality.
 */
public abstract class BasicBuilding implements Building {
    protected String name;
    protected List<String> sources;
    protected List<Request> requestQueue;
    protected Request currentRequest;
    protected int remainingTime;
    protected Map<String, Integer> storage;
    protected Coordinate location;
    protected boolean markedForRemoval = false;  // Flag to indicate if the building is marked for removal
    protected BasicSimulation simulation;

    /**
     * Constructs a BasicBuilding.
     *
     * @param name    the building name.
     * @param sources list of source building names.
     */
    public BasicBuilding(String name, List<String> sources) {
        this.name = name;
        this.sources = new ArrayList<>(sources);
        this.requestQueue = new ArrayList<>();
        this.currentRequest = null;
        this.remainingTime = 0;
        this.storage = new HashMap<>();
    }

    @Override
    public Coordinate getLocation() {
        return location;
    }

    @Override
    public void setLocation(Coordinate location) {
        this.location = location;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getSources() {
        return new ArrayList<>(sources);
    }

    @Override
    public void setSimulation(BasicSimulation simulation) {
        this.simulation = simulation;
    }

    @Override
    public BasicSimulation getSimulation() {
        return simulation;
    }

    @Override
    public void addSource(String source) {
        if (!sources.contains(source)) {
            sources.add(source);
        }
    }


    @Override
    public void addRequest(Request request) {
        // idle function, to be implemented by subclasses
        requestQueue.add(request);
    }

    @Override
    public int getQueueLength() {
        // Return the total number of requests (queue + current if any)
        return requestQueue.size() + (currentRequest != null ? 1 : 0);
    }

    @Override
    public void deliverItem(String item, int quantity) {
        // Add the delivered item to storage
        storage.put(item, storage.getOrDefault(item, 0) + quantity);
    }

    @Override
    public List<Request> step(int currentTimeStep, int verbosity){
        // idle function for abstraction
        return new ArrayList<>();
    }

    @Override
    public Request selectNextRequest(int currentTimeStep, int verbosity){
        // idle function for abstraction
        return null;
    }


    @Override
    public void processIngredients(String item) {
        int verbosity = simulation.getVerbosity();
        Map<String, Recipe> recipeMap = simulation.getRecipes();
        Recipe recipe = recipeMap.get(item);
        Map<String, Integer> ingredients = recipe.getIngredients();
        Map<String, Building> buildingMap = simulation.getBuildings();
        
        if (verbosity >= 2) {
            System.out.println("[source selection]: " + this.getName() + " (qlen) has request for " + item + " on " + simulation.getCurrentTimeStep());
        }
        
        // If is mine recipe, return directly
        if (ingredients.isEmpty()) {
            return;
        }
        
        // The trick: we need to access ingredients in the original JSON order
        // We can do this by getting the ordered list from the Recipe object
        List<String> orderedIngredients = recipe.getOrderedIngredientNames();
        int idx = 0;
        
        // Process ingredients in their original order from the JSON
        for (String ingredient : orderedIngredients) {
            // Get total required amount from recipe
            int count = ingredients.get(ingredient);
            // Process each quantity of this ingredient
            for (int i = 0; i < count; i++) {
                if (verbosity >= 2) {
                    System.out.println("[" + this.getName() + ":" + item + ":" + idx + "] For ingredient " + ingredient);
                }
                idx++;

                Recipe r = simulation.getRecipe(ingredient);
                // Find source building with the smallest queue
                int minRequest = Integer.MAX_VALUE;
                Building resBuilding = null;
                
                for (String s : sources) {
                    Building b = buildingMap.get(s);
                    if (b.canProduce(r.getOutput())) {
                        // if the building is marked for removal, skip it
                        if (b.isMarkedForRemoval()){
                            continue;
                        }
                        int queueLength;
                        // Special case for storage buildings with items in stock count as negative queue length
                        if (b instanceof StorageBuilding storageBuilding) {
                            String storedItem = storageBuilding.getStoredItem();
                            if (storedItem.equals(ingredient) && storageBuilding.getCurrentStorage() > 0) {
                                // Negative queue length based on number of items in stock
                                queueLength = -storageBuilding.getCurrentStorage();
                            } else {
                                queueLength = b.getQueueLength();
                            }
                        } else {
                            queueLength = b.getQueueLength();
                        }
                
                        if (verbosity >= 2) {
                            System.out.println("    " + b.getName() + ": " + b.getQueueLength());
                        }
                        
                        if (queueLength < minRequest) {
                            minRequest = queueLength;
                            resBuilding = b;
                        }
                    }
                }
                
                if (resBuilding == null) {
                    throw new RuntimeException("No building can produce " + ingredient);
                }
                
                if (verbosity >= 2) {
                    System.out.println("    Selecting " + resBuilding.getName());
                }
                
                if (verbosity >= 1) {
                    System.out.println("[ingredient assignment]: " + ingredient + " assigned to " + 
                                    resBuilding.getName() + " to deliver to " + this.getName());
                }
                
                Request newRequest = new Request(simulation.getNextRequestId(), r, this, false, simulation.getCurrentTimeStep());
                resBuilding.addRequest(newRequest);
                
                // DFS: process this ingredient's dependencies before moving to the next
                resBuilding.processIngredients(r.getOutput());
            }
        }
    }

    @Override
    public boolean canBeRemovedImmediately(){
        // Check if the building can be removed immediately
        // idle for abstraction
        return requestQueue.isEmpty() && currentRequest == null;
    }

    @Override
    public void markForRemoval() {
        this.markedForRemoval = true;
    }

    @Override
    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    @Override
    public boolean isViable() {
        // idle function for abstraction
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Print building name
        sb.append("Name: ").append(name).append("\n");

        // Print building type
        sb.append("Type: ").append(getClass().getSimpleName()).append("\n");

        // Print viability status
        sb.append("Viable: ").append(isViable()).append("\n");

        // Print location
        if (location != null) {
            sb.append("LocationX: ").append(location.getX()).append("\n");
            sb.append("LocationY: ").append(location.getY()).append("\n");
        } else {
            sb.append("Location: Not set\n");
        }

        // Print sources
        if (sources.isEmpty()) {
            sb.append("Sources: []\n");
        } else {
            sb.append("Sources: ").append(sources).append("\n");
        }

        // Print the request queue
        if (requestQueue.isEmpty()) {
            sb.append("RequestQueue: []\n");
        } else {
            sb.append("RequestQueue:\n");
            for (Request r : requestQueue) {
                sb.append("  - Request ID: ").append(r.getId())
                .append(", Output: ").append(r.getRecipe().getOutput())
                .append(", Status: ").append(r.getStatus())
                .append(", Requestor=").append((r.getRequestor() == null ? " user" : r.getRequestor().getName()))
                .append("\n");
            }
        }

        // Print the current request being processed, if any
        if (currentRequest != null) {
            sb.append("CurrentRequest: ")
            .append("ID=").append(currentRequest.getId())
            .append(", Output=").append(currentRequest.getRecipe().getOutput())
            .append(", Status=").append(currentRequest.getStatus())
            .append(", Requestor=").append((currentRequest.getRequestor() == null ? "user" : currentRequest.getRequestor().getName()))
            .append("\n");
        } else {
            sb.append("CurrentRequest: None\n");
        }

        // Print the building's storage
        sb.append("Storage: ").append(storage).append("\n");

        // Add a blank line for clarity
        sb.append("\n");
        
        return sb.toString();
    }
}