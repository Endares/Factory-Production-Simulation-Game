package edu.duke.ece651.hw2.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a storage building in the simulation.
 * Storage buildings can store a specific type of item up to a maximum capacity.
 * They periodically make requests to source buildings to maintain their stock.
 */
public class StorageBuilding extends BasicBuilding {
    private String storedItem;
    private int capacity;
    private float priority;
    private int outstandingRequests;

    /**
     * Constructs a StorageBuilding.
     *
     * @param name     the building name.
     * @param storedItem  the item type this storage can hold.
     * @param capacity the maximum number of items that can be stored.
     * @param priority the priority value used to determine request frequency.
     * @param sources  list of source building names.
     */
    public StorageBuilding(String name, String storedItem, int capacity, float priority, List<String> sources) {
        super(name, sources);
        this.storedItem = storedItem;
        this.capacity = capacity;
        this.priority = priority;
        this.outstandingRequests = 0;
    }

    @Override
    public String toString() {
        return super.toString() + "StoredItem: " + storedItem + "\n" +
                "Capacity: " + capacity + "\n" +
                "Priority: " + priority + "\n" +
                "OutstandingRequests: " + outstandingRequests + "\n";
    }

    /**
     * Gets the item type that this storage building stores.
     *
     * @return the stored item type.
     */
    public String getStoredItem() {
        return storedItem;
    }

    /**
     * Gets the maximum capacity of this storage building.
     *
     * @return the capacity.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Gets the priority value that determines request frequency.
     *
     * @return the priority value.
     */
    public float getPriority() {
        return priority;
    }

    /**
     * Gets the current amount of items in storage.
     *
     * @return the current storage amount.
     */
    public int getCurrentStorage() {
        return storage.getOrDefault(storedItem, 0);
    }

    /**
     * Checks if this storage building can produce the specified item.
     *
     * @param itemName the item name to check.
     * @return true if this storage can produce the item, false otherwise.
     */
    @Override
    public boolean canProduce(String itemName) {
        return storedItem.equals(itemName);
    }

    @Override
    public boolean canBeRemovedImmediately() {
        // 存储建筑可以立即移除的条件：
        // 1. 队列中没有请求 2. 存储中没有物品 3. 没有未完成的请求 !较为苛刻
        return requestQueue.isEmpty() && getCurrentStorage() == 0 && outstandingRequests == 0;
    }

    /**
     * For a storage building, the provided output is its stored item.
     *
     * @return list containing the stored item.
     */
    @Override
    public List<String> getProvidedOutputs() {
        List<String> outputs = new ArrayList<>();
        outputs.add(storedItem);
        return outputs;
    }

    @Override
    public void deliverItem(String item, int quantity) {
        // Only decrement outstandingRequests if the item is the one we store
        if (item.equals(storedItem)) {
            outstandingRequests = Math.max(0, outstandingRequests - quantity);
        }
        
        // Call the parent implementation to add to storage
        super.deliverItem(item, quantity);
    }

    /**
     * Adds a request to this storage building's queue.
     * If the requested item is available in storage, marks it as ready for immediate delivery.
     * Otherwise, the request will stay in queue until items become available.
     *
     * @param request the Request object.
     */
    @Override
    public void addRequest(Request request) {
        // Verify this request is for the item type we store
        String requestedItem = request.getRecipe().getOutput();
        if (!requestedItem.equals(storedItem)) {
            throw new IllegalArgumentException("Storage building " + name + " cannot store item type: " + requestedItem);
        }
    
        // Check if we have this item in storage
        int available = storage.getOrDefault(storedItem, 0);
        
        // If we have the item available in storage, deliver it immediately
        if (available > 0) {
            // Decrease available count
            storage.put(storedItem, available - 1);
            
            // Deliver to requestor if not a user request
            if (!request.isUserRequest()) {
                Building requestor = request.getRequestor();
                // requestor.deliverItem(storedItem, 1);
                // System.out.println("Using existing item from storage: " + storedItem + 
                //                 " to fulfill request from " + requestor.getName() 
                //                 + " to " + this.getName() + " [storage request]");
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
                    simulation.scheduleDelivery(this, requestor, storedItem, 1, 
                                            simulation.getCurrentTimeStep() + transitTime);
                } else { //transitTime is 0
                    // if adjacent, deliver immediately
                    requestor.deliverItem(storedItem, 1);
                    
                    if (simulation.getVerbosity() >= 1) {
                        System.out.println("[ingredient delivered]: " + storedItem + 
                                        " to " + requestor.getName() + 
                                        " from " + name + 
                                        " on cycle " + (simulation.getCurrentTimeStep()+1));
                        if (requestor instanceof FactoryBuilding factory) {
                            simulation.checkReadyRecipesAtFactory(factory);
                        }
                    }
                }
            }
            
            // Mark as completed
            request.setStatus(RequestStatus.COMPLETED);
        }
        
        // Always add to queue - completed ones will be collected in step
        requestQueue.add(request);
    }


    /**
     * Processes a time step for this storage building.
     * Handles completed requests, fulfills pending requests from storage,
     * and makes periodic restocking requests.
     *
     * @param currentTimeStep the current time step in the simulation.
     * @param verbosity the verbosity level.
     * @return list of completed requests during this time step.
     */
    @Override
    public List<Request> step(int currentTimeStep, int verbosity) {
        List<Request> completedRequests = new ArrayList<>(); 
        // 1. First, process already completed requests from previous addRequest calls
        Iterator<Request> iterator = requestQueue.iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();
            
            if (request.getStatus() == RequestStatus.COMPLETED) {
                completedRequests.add(request);
                iterator.remove();
            }
        }
        // 2. Try to fulfill pending requests from storage
        iterator = requestQueue.iterator();
        while (iterator.hasNext() && storage.getOrDefault(storedItem, 0) > 0) {
            Request request = iterator.next();
            
            // Decrease storage
            int available = storage.getOrDefault(storedItem, 0);
            storage.put(storedItem, available - 1);
            
            // Mark as completed
            request.setStatus(RequestStatus.COMPLETED);
            completedRequests.add(request);
            
            // Deliver to requestor if not a user request
            if (!request.isUserRequest()) {
                Building requestor = request.getRequestor();
                // requestor.deliverItem(storedItem, 1);
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
                    simulation.scheduleDelivery(this, requestor, storedItem, 1, 
                                            currentTimeStep + transitTime);
                } else {
                    // if adjacent, deliver immediately
                    requestor.deliverItem(storedItem, 1);
                    
                    if (verbosity >= 1) {
                        System.out.println("[ingredient delivered]: " + storedItem + 
                                        " to " + requestor.getName() + 
                                        " from " + name + 
                                        " on cycle " + (currentTimeStep+1));
                        if (requestor instanceof FactoryBuilding factory) {
                            simulation.checkReadyRecipesAtFactory(factory);
                        }
                    }
                }
            } else {
                // User request
                System.out.println("[order complete] Order " + request.getId() + 
                                " completed (" + storedItem + ") at time " + (currentTimeStep + 1)  + " [storage request]");
            }
            
            // Remove from queue
            iterator.remove();
        }
        // 3. Check if we need to make a replenishment request
        if (shouldMakeRequest(currentTimeStep)) {
            makeReplenishmentRequest(currentTimeStep, verbosity);
        }
        
        return completedRequests;
    }

    /**
     * Checks which recipes are ready at a factory after delivering an item.
     * Used for verbosity reporting at level 1 or higher.
     * 
     * @param factory the factory to check
     * @param verbosity the current verbosity level
     */
    private void checkReadyRecipesAtFactory(FactoryBuilding factory, int verbosity) {
        for (int i = 0; i < factory.requestQueue.size(); i++) {
            Request req = factory.requestQueue.get(i);
            Recipe recipe = req.getRecipe();
            boolean isReady = true;
            
            for (Map.Entry<String, Integer> entry : recipe.getIngredients().entrySet()) {
                String ingredient = entry.getKey();
                int required = entry.getValue();
                int available = factory.storage.getOrDefault(ingredient, 0);
                
                if (available < required) {
                    isReady = false;
                    break;
                }
            }
            
            if (isReady) {
                System.out.println("    " + i + ": " + recipe.getOutput() + " is ready "  + " [storage request]");
            }
        }
    }

    /**
     * Makes a replenishment request to a source building.
     * Called periodically when the storage needs to be restocked.
     */
    private void makeReplenishmentRequest(int currentTimeStep, int verbosity) { 
        // Don't make a request if R <= 0
        int R = calculateRemainingSpace();
        if (R <= 0) return;
        
        // Get the recipe for the stored item
        Recipe recipe = simulation.getRecipe(storedItem);
        if (recipe == null) return;
        // Check if the connection is valid
        
        // String connectivityErrors = simulation.checkBuildingConnections(this, storedItem);
        // System.out.println(connectivityErrors);
        // if (connectivityErrors != null) {
        //     System.out.println("Storage Building" + this.getName() +
        //         "Cannot process request due to missing connections: " + connectivityErrors);
        //     return;
        // }
        // Call processIngredients to handle the request
        processIngredients(storedItem);
    }


    /**
     * Calculates the frequency at which this storage building should make restock requests.
     *
     * @return the frequency (in cycles).
     */
    private int calculateRequestFrequency() {
        int R = calculateRemainingSpace();
        if (R <= 0) {
            return 0;  // No requests needed
        }
        
        // Calculate F = ceil(T / (2 * R * P))
        double frequency = Math.ceil(capacity / (2.0 * R * priority));
        return (int) frequency;
    }



    /**
     * Calculates the remaining space in storage, considering current inventory,
     * outstanding requests, and queue size.
     *
     * @return the effective remaining space known as R
     */
    private int calculateRemainingSpace() {
        int current = getCurrentStorage();
        int queueSize = requestQueue.size();
        // if currentRequest is not null, it means there is an ongoing request
        if (currentRequest != null) {
            queueSize++;
        }
        return capacity - current - outstandingRequests + queueSize;
    }

    /**
     * Determines if the storage building should make a restock request in the current cycle.
     *
     * @param currentTimeStep the current time step.
     * @return true if a request should be made, false otherwise.
     */
    private boolean shouldMakeRequest(int currentTimeStep) {
        int frequency = calculateRequestFrequency();
        if (frequency == 0) {
            return false;  // No space or frequency is zero
        }
        return currentTimeStep % frequency == 0;
    }


    /**
     * Overrides the processIngredients method for StorageBuilding.
     * Storage buildings don't process complex recipes - they just request their stored item.
     */
    @Override
    public void processIngredients(String item) {
        // Verify this is the item we store
        if (!item.equals(storedItem)) {
            throw new IllegalArgumentException("Storage building " + name + " cannot process item: " + item);
        }
        
        // 守卫检查：遍历 requestQueue
        // 如果找到与该 item 相关的 user request 且状态为 COMPLETED，就直接返回，
        // 避免继续递归分配 ingredient 请求
        for (Request req : requestQueue) {
            if (req.isUserRequest() 
                && req.getStatus() == RequestStatus.COMPLETED 
                && req.getRecipe().getOutput().equals(item)) {
                // 找到已完成的用户请求，直接退出，防止重复分配
                return;
            }
        }
        
        int verbosity = simulation.getVerbosity();
        Map<String, Building> buildingMap = simulation.getBuildings();
        Recipe recipe = simulation.getRecipe(storedItem);
        
        if (verbosity >= 2) {
            System.out.println("[source selection]: " + this.getName() + " (qlen) has request for " + storedItem 
                + " on cycle " + simulation.getCurrentTimeStep() + " [storage request]");
        }
        
        // Find source building with the smallest queue
        int minRequest = Integer.MAX_VALUE;
        Building resBuilding = null;
        
        for (String s : sources) {
            Building b = buildingMap.get(s);
            if (b.canProduce(storedItem)) {
                int queueLength;
                if (b.isMarkedForRemoval()){
                    continue;
                }
                // Special case for storage buildings with items in stock
                if (b instanceof StorageBuilding storageBuilding) {
                    String stored_Item = storageBuilding.getStoredItem();
                    if (stored_Item.equals(this.storedItem) && storageBuilding.getCurrentStorage() > 0) {
                        // Negative queue length based on number of items in stock
                        queueLength = -storageBuilding.getCurrentStorage();
                    } else {
                        queueLength = b.getQueueLength();
                    }
                } else {
                    queueLength = b.getQueueLength();
                }

                if (verbosity >= 2) {
                    System.out.println("    " + b.getName() + ": " + b.getQueueLength() + " [storage request]");
                }
                
                if (queueLength < minRequest) {
                    minRequest = queueLength;
                    resBuilding = b;
                }
            }
        }
        
        if (resBuilding == null) {
            throw new RuntimeException("No building can produce " + storedItem);
        }
        
        if (verbosity >= 2) {
            System.out.println("    Selecting " + resBuilding.getName() + " [storage request]");
        }
        
        if (verbosity >= 1) {
            System.out.println("[ingredient assignment]: " + storedItem + " assigned to " + 
                            resBuilding.getName() + " to deliver to " + this.getName() + " [storage request]");
        }
        
        // Create a single request for the stored item
        Request newRequest = new Request(simulation.getNextRequestId(), recipe, this, false, simulation.getCurrentTimeStep());
        resBuilding.addRequest(newRequest);
        
        // Always call processIngredients on the source, regardless of building type
        resBuilding.processIngredients(storedItem);
        
        // Increment outstanding requests counter
        outstandingRequests++;
    }

    @Override
    public boolean isViable() {
        // 存储建筑需要能够获取它存储的物品
        boolean canGetStoredItem = false;
        
        for (String sourceName : sources) {
            Building source = simulation.getBuildings().get(sourceName);
            if (source != null && source.canProduce(storedItem)) {
                // 检查是否有有效的路径
                if (simulation.getRoadMap().getShortestDistance(source, this) >= 0) {
                    canGetStoredItem = true;
                    break;
                }
            }
        }
        
        return canGetStoredItem;
    }
}