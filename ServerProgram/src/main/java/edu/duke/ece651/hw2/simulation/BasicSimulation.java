package edu.duke.ece651.hw2.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Basic implementation of the Simulation interface.
 */
public class BasicSimulation implements Simulation {
    private int currentTimeStep;
    private int timeRate;
    private Map<String, Building> buildings;
    private Map<String, Recipe> recipes;
    private Map<String, BuildingType> buildingTypes;
    private int verbosityLevel;
    private int nextRequestId;
    private List<Request> userRequests;
    private CommandProcessor commandProcessor;
    private RoadMap roadMap;
    // delayed delivery
    private List<DelayedDelivery> delayedDeliveries = new ArrayList<>();
    // buildabletypes
    private Map<String, BuildableType> buildableTypes = new HashMap<>();


    /**
     * Constructs a BasicSimulation.
     *
     * @param buildings     map of building names to Building objects.
     * @param recipes       map of recipe outputs to Recipe objects.
     * @param buildingTypes map of building type names to BuildingType objects.
     */
    public BasicSimulation(Map<String, Building> buildings, Map<String, Recipe> recipes, Map<String, BuildingType> buildingTypes, Map<String, BuildableType> buildableTypes) {
        this.currentTimeStep = 0;
        this.timeRate = 1; // 初始默认时间流速为 1 steps/sec
        this.buildings = buildings;
        this.recipes = recipes;
        this.buildingTypes = buildingTypes;
        this.verbosityLevel = 0;
        this.nextRequestId = 0;
        this.userRequests = new ArrayList<>();
        this.commandProcessor = new CommandProcessor(this);
        this.roadMap = new RoadMap();
        this.delayedDeliveries = new ArrayList<>();
        this.buildableTypes = buildableTypes;

        // 将已有坐标的建筑加入 roadMap
        for(Building b : buildings.values()){
            if(b.getLocation() != null){
                roadMap.addBuilding(b);
            }
        }
    }

    public BasicSimulation(Map<String, Building> buildings, Map<String, Recipe> recipes, Map<String, BuildingType> buildingTypes) {
        this.currentTimeStep = 0;
        this.timeRate = 1; // 初始默认时间流速为 1 steps/sec
        this.buildings = buildings;
        this.recipes = recipes;
        this.buildingTypes = buildingTypes;
        this.verbosityLevel = 0;
        this.nextRequestId = 0;
        this.userRequests = new ArrayList<>();
        this.commandProcessor = new CommandProcessor(this);
        this.roadMap = new RoadMap();

        // 将已有坐标的建筑加入 roadMap
        for(Building b : buildings.values()){
            if(b.getLocation() != null){
                roadMap.addBuilding(b);
            }
        }
    }

    // Create a static factory method that creates and initializes the simulation
    public static BasicSimulation createSimulation(Map<String, Building> buildings, Map<String, Recipe> recipes, Map<String, BuildingType> buildingTypes, Map<String, BuildableType> buildableTypes) {
        BasicSimulation sim = new BasicSimulation(buildings, recipes, buildingTypes, buildableTypes);
        
        // Simulation Di into Buildings
        for (Building building : buildings.values()) {
            building.setSimulation(sim);
        }
        
        return sim;
    }

    // Create a static factory method that creates and initializes the simulation
    public static BasicSimulation createSimulation(Map<String, Building> buildings, Map<String, Recipe> recipes, Map<String, BuildingType> buildingTypes) {
        BasicSimulation sim = new BasicSimulation(buildings, recipes, buildingTypes);

        // Simulation Di into Buildings
        for (Building building : buildings.values()) {
            building.setSimulation(sim);
        }

        return sim;
    }

    /**
     * Adds a user request to the simulation.
     *
     * @param request the Request object.
     */
    public void addRequest(Request request) {
        userRequests.add(request);
        nextRequestId++;
    }

    /**
     * Gets the next unique request ID
     * for ++
     *
     * @return the next request ID.
     */
    public int getNextRequestId() {
        return nextRequestId++;
    }

    /**
     * Gets the next unique request ID.
     *
     * @return the next request ID.
     */
    public int gettheNextRequestId() {
        return nextRequestId;
    }

    @Override
    public void processCommand(String command) {
        commandProcessor.processCommand(command);
    }

    /**
     * Schedules an item delivery for a future time step.
     *
     * @param destination the building that will receive the item.
     * @param item the item type to be delivered.
     * @param quantity the quantity of the item.
     * @param deliveryTime the time step when the delivery should arrive.
     */
    public void scheduleDelivery(Building source, Building destination, String item, int quantity, int deliveryTime) {
        delayedDeliveries.add(new DelayedDelivery(source, destination, item, quantity, deliveryTime));
    }

    @Override
    public void step(int steps) {
        if (steps < 0) {
            System.err.println("Error: Steps must be at least 1");
            return;
        }
        
        for (int i = 0; i < steps; i++) {
            processSingleTimeStep();
            currentTimeStep++;
        }
    }

    public static List<Building> getTopOrder(Map<String, Building> buildings) {
        // 初始化每个建筑的入度和邻接表
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> graph = new HashMap<>();
        for (String name : buildings.keySet()) {
            inDegree.put(name, 0);
            graph.put(name, new ArrayList<>());
        }

        // 对于每个建筑 A，若其 sources 中包含 B，则添加一条边 B -> A，并增加 A 的入度
        for (Building building : buildings.values()) {
            for (String source : building.getSources()) {
                if (buildings.containsKey(source)) {
                    graph.get(source).add(building.getName());
                    inDegree.put(building.getName(), inDegree.get(building.getName()) + 1);
                }
            }
        }

        // 将所有入度为 0 的建筑加入队列
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<Building> sortedBuildings = new ArrayList<>();
        // 拓扑排序过程
        while (!queue.isEmpty()) {
            String currentName = queue.poll();
            sortedBuildings.add(buildings.get(currentName));
            for (String neighbor : graph.get(currentName)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        return sortedBuildings;
    }
    
    /**
     * Processes a single time step of the simulation.
     * Calls step() on all buildings and processes completed requests.
     */
    private void processSingleTimeStep() {
        // Process delayed deliveries that are due at the current time step
        processDelayedDeliveries();
        List<Request> completedRequests = new ArrayList<>();
        List<Building> orderedBuildings = getTopOrder(buildings);

        // Process all buildings in order
        for (Building building : orderedBuildings) {
            // check which building goes wrong
            System.out.println("Processing building: " + building.getName());
            List<Request> buildingCompletedRequests = building.step(currentTimeStep, verbosityLevel);
            completedRequests.addAll(buildingCompletedRequests);
        }

        // Process completed requests
        for (Request request : completedRequests) {
            if (request.isUserRequest()) {
                userRequests.remove(request);
            }
        }

        // Check for buildings marked for removal, if marked and can be removed, remove them
        checkBuildingsMarkedForRemoval();
    }

    /**
     * Processes all delayed deliveries that are due at the current time step.
     * Removes processed deliveries from the queue.
     */
    private void processDelayedDeliveries() {
        Iterator<DelayedDelivery> iterator = delayedDeliveries.iterator();
        while (iterator.hasNext()) {
            DelayedDelivery delivery = iterator.next();
            if (delivery.getDeliveryTime() <= currentTimeStep) {
                // 物品到达目的地，实际递送
                delivery.getDestination().deliverItem(delivery.getItem(), delivery.getQuantity());
                
                // 根据需要报告递送信息
                if (verbosityLevel >= 1) {
                    System.out.println("[ingredient delivered]: " + delivery.getItem() + 
                                    " to " + delivery.getDestination().getName() + 
                                    " from " + delivery.getSource().getName() + 
                                    " on cycle " + (currentTimeStep + 1));
                    
                    // 如果目标是工厂，检查就绪状态
                    if (delivery.getDestination() instanceof FactoryBuilding factory) {
                        checkReadyRecipesAtFactory(factory);
                    }
                }
                
                iterator.remove();
            }
        }
    }

    /*
     * Checks which buildings are marked for removal and can be removed immediately.
     * If a building is marked for removal and can be removed, it is removed from the simulation.
     */
    private void checkBuildingsMarkedForRemoval() {
        List<Building> buildingsToRemove = new ArrayList<>();
        
        for (Building building : buildings.values()) {
            if (building.isMarkedForRemoval() && building.canBeRemovedImmediately()) {
                buildingsToRemove.add(building);
            }
        }
        
        for (Building building : buildingsToRemove) {
            performRemoveBuilding(building);
        }
    }


    /**
     * Checks which recipes are ready at a factory after delivering an item.
     * Used for verbosity reporting at level 1 or higher.
     * 
     * @param factory the factory to check
     */
    public void checkReadyRecipesAtFactory(FactoryBuilding factory) {
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
                System.out.println("    " + i + ": " + recipe.getOutput() + " is ready");
            }
        }
    }

    @Override
    public void finish() {
        // Final simulation processing can be added here.
        while (!userRequests.isEmpty()) {
            processSingleTimeStep();
            currentTimeStep++;
        }
        System.out.println("Final simulation time: " + currentTimeStep);
    }

    @Override
    public int getCurrentTimeStep() {
        return currentTimeStep;
    }

    @Override
    public void setVerbosity(int level) {
        this.verbosityLevel = level;
    }

    public int getVerbosity() {
        return verbosityLevel;
    }

    /**
     * Runs the interactive simulation loop.
     * <p>
     * The prompt is printed as "[currentTimeStep]>" (e.g. "0>" initially).
     * Commands are read line by line from standard input.
     */
    public void runInteractive() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(currentTimeStep + "> ");
            String input = scanner.nextLine();
            if (input.trim().isEmpty()) {
                continue;
            }
            // The "finish" command terminates the simulation.
            if (input.trim().equals("finish")) {
                processCommand(input);
                break;
            }
            processCommand(input);
        }
        scanner.close();
    }

    /**
     * Handles a user request for an item from a building.
     * 
     * @param itemName the name of the item requested.
     * @param buildingName the name of the building to request from.
     * @throws SimulationException if the request is invalid.
     */
    public void handleRequestCommand(String itemName, String buildingName) throws SimulationException {
        // Validate the building and the recipes relationship
        if (!buildings.containsKey(buildingName)) {
            throw new SimulationException("Building '" + buildingName + "' does not exist");
        }
        
        Building building = buildings.get(buildingName); // get building 
        
        if (!recipes.containsKey(itemName)) {
            throw new SimulationException("Recipe '" + itemName + "' does not exist");
        }
        
        Recipe recipe = recipes.get(itemName); // get recipe

        if (!building.canProduce(itemName)) {
            throw new SimulationException("Building '" + buildingName + "' cannot produce '" + itemName + "'");
        }
        String connectivityErrors = checkBuildingConnections(building, itemName);
        if (connectivityErrors != null) {
            throw new SimulationException("Cannot process request due to missing connections:\n" + connectivityErrors);
        }
        
        // Create the user request
        Request userRequest = new Request(getNextRequestId(), recipe, null, true, currentTimeStep);
        userRequests.add(userRequest);
        building.addRequest(userRequest);
        // process ingredients recursively
        building.processIngredients(itemName);
        System.out.println("Added request for '" + itemName + "' from '" + buildingName + "'");
    }

    /**
     * Checks if a building has valid connections to all sources needed for producing an item.
     * Uses the same recursive pattern as processIngredients.
     * 
     * @param building The building that will handle the request
     * @param itemName The item to be produced
     * @return A string containing all missing connections, or null if all connections are valid
     */
    public String checkBuildingConnections(Building building, String itemName) {
        StringBuilder errors = new StringBuilder();
        // Use the same recursive logic as processIngredients
        validateConnections(building, itemName, errors);
        
        return !errors.isEmpty() ? errors.toString() : null;
    }

    /**
     * Validates connections between buildings following the same pattern as processIngredients.
     * The key difference is this method only validates connections without creating requests.
     */
    public void validateConnections(Building building, String item, StringBuilder errors) {
        // Avoid processing the same building-item combination multiple times
//        String key = building.getName() + ":" + item;
        System.out.println("Validating connections for building: " + building.getName() + ", item: " + item);
        Recipe recipe = recipes.get(item);
        Map<String, Integer> ingredients = recipe.getIngredients();
        
        // If it's a mine recipe (no ingredients), return directly
        if (ingredients.isEmpty()) {
            return;
        }
        
        // Process ingredients in their original order from the JSON
        List<String> orderedIngredients = recipe.getOrderedIngredientNames();
        
        for (String ingredient : orderedIngredients) {
            int count = ingredients.get(ingredient);
            
            // Process each quantity of this ingredient
            for (int i = 0; i < count; i++) {
//                Recipe r = recipes.get(ingredient);
                
                // Find source building with valid connection
                Building resBuilding = null;
                
                for (String s : building.getSources()) {
                    Building b = buildings.get(s);
                    if (b == null) {
                        errors.append("Error: Source building '").append(s)
                            .append("' referenced by '").append(building.getName())
                            .append("' does not exist.\n");
                        continue;
                    }
                    
                    if (b.canProduce(ingredient)) {
                        // if the building is marked for removal, skip it
                        if (b.isMarkedForRemoval()) {
                            continue;
                        }
                        // Check road connection
                        int distance = roadMap.getShortestDistance(b, building);
                        if (distance >= 0) {
                            resBuilding = b;
                            break; // Found a valid source with connection
                        } else {
                            errors.append("Error: No path exists from '").append(building.getName())
                                .append("' to source '").append(s)
                                .append("' needed for ingredient '").append(ingredient).append("'.\n");
                        }
                    }
                }
                
                if (resBuilding == null) {
                    errors.append("Error: No source for building '").append(building.getName())
                        .append("' can produce '").append(ingredient).append("'.\n");
                    continue;
                }
                
                // DFS: validate this ingredient's connections recursively
                validateConnections(resBuilding, ingredient, errors);
            }
        }
    }

    @Override
    public void buildBuilding(String typeName, int x, int y) throws SimulationException {
        // 1. 检查坐标是否已被占用
        Coordinate location = new Coordinate(x, y);
        if (roadMap.getBuildingLocations().containsKey(location) || roadMap.getRoads().containsKey(location)) {
            throw new SimulationException("Location (" + x + ", " + y + ") is already occupied");
        }
        
        // 2. 从buildableTypes中查找对应的类型
        BuildableType buildableType = buildableTypes.get(typeName);
        if (buildableType == null) {
            throw new SimulationException("Building type '" + typeName + "' does not exist");
        }
        
        // 3. 根据类型创建建筑
        String buildingName = generateUniqueBuildingName(typeName);
        Building newBuilding;
        String buildingType = buildableType.getType();
        JSONObject info = buildableType.getInfo();
        
        switch (buildingType) {
            case "factory" -> {
                // 创建工厂建筑
                JSONArray recipesArray = info.getJSONArray("recipes");
                List<String> recipesList = new ArrayList<>();
                for (int i = 0; i < recipesArray.length(); i++) {
                    recipesList.add(recipesArray.getString(i));
                }
                BuildingType bt = new BuildingType(buildingName, recipesList);
                newBuilding = new FactoryBuilding(buildingName, bt, new ArrayList<>());
            }
            case "storage" -> {
                // 创建存储建筑
                String storedItem = info.getString("stores");
                int capacity = info.getInt("capacity");
                float priority = (float) info.getDouble("priority");
                newBuilding = new StorageBuilding(buildingName, storedItem, capacity, priority, new ArrayList<>());
            }
            case "mine" -> {
                // 创建矿山建筑
                String minedItem = info.getString("mine");
                Recipe mineRecipe = recipes.get(minedItem);
                if (mineRecipe == null) {
                    throw new SimulationException("Recipe for mined item '" + minedItem + "' does not exist");
                }
                newBuilding = new MineBuilding(buildingName, minedItem, mineRecipe, new ArrayList<>());
            }
            case "drone" -> {
                // 创建无人机建筑
                newBuilding = new DroneBuilding(buildingName, new ArrayList<>());
            }
            default -> throw new SimulationException("Unknown building type: " + buildingType);
        }
        
        // 4. 设置建筑属性
        if (newBuilding != null) {
            newBuilding.setLocation(location);
        }
        else {
            throw new SimulationException("Failed to create a new building of type '" + typeName + "'");
        }
        // Dependency Injection
        newBuilding.setSimulation(this);
        
        // 5. 将建筑添加到模拟系统
        buildings.put(buildingName, newBuilding);
        roadMap.addBuilding(newBuilding);
        
        System.out.println("Building " + buildingName + " of type " + typeName + " created at (" + x + ", " + y + ")");
    }

    /*
     * Generates a unique name for a new building based on its type name.
     * 
     * @typeName The type name of the building.
     * @return A unique name for the building: typeName + "_" + count.
     */
    private String generateUniqueBuildingName(String typeName) {
        int count = 1;
        String baseName = typeName.replaceAll("[^a-zA-Z0-9]", "_");
        String name = baseName + "_" + count;
        
        while (buildings.containsKey(name)) {
            count++;
            name = baseName + "_" + count;
        }
        
        return name;
    }


    @Override
    public void removeBuilding(String buildingName) throws SimulationException {
        Building building = buildings.get(buildingName);
        if (building == null) {
            throw new SimulationException("Building '" + buildingName + "' does not exist");
        }
        
        if (building.canBeRemovedImmediately()) {
            performRemoveBuilding(building);
        } else {
            building.markForRemoval();
            System.out.println("Building '" + buildingName + "' has been marked for removal");
        }
    }

    // 执行建筑物的移除
    private void performRemoveBuilding(Building building) {
        String buildingName = building.getName();
        Coordinate location = building.getLocation();
        
        // 1. 移除所有与该建筑相关的连接
        List<Pair<Building, Building>> connectionsToRemove = new ArrayList<>();
        
        // 找出所有涉及此建筑的连接
        for (Pair<Building, Building> connection : roadMap.getConnections()) {
            if (connection.first.equals(building) || connection.second.equals(building)) {
                connectionsToRemove.add(connection);
            }
        }
        
        // 删除这些连接
        for (Pair<Building, Building> connection : connectionsToRemove) {
            roadMap.complexRemoval(connection.first, connection.second);
        }
        
        // 2. 从buildings映射中移除
        buildings.remove(buildingName);
        
        // 3. 从roadMap中移除
        roadMap.removeBuilding(location);

        // 4. 从roadMap种删除路格
        roadMap.removeRoad(location);
        
        System.out.println("Building '" + buildingName + "' has been removed");
    }

    public Map<String, Building> getBuildings() {
        return buildings;
    }
    
    public Map<String, Recipe> getRecipes() {
        return recipes;
    }
    public Recipe getRecipe(String id) {
        return recipes.get(id);
    }
    public RoadMap getRoadMap() {
        return roadMap;
    }

    // 新增：连接两建筑（对应新指令 connect 'SOURCE_NAME' to 'DEST_NAME'）
    public void connectBuildings(String sourceName, String destName) throws SimulationException {
        if (!buildings.containsKey(sourceName)) {
            throw new SimulationException("Source building '" + sourceName + "' does not exist");
        }
        if (!buildings.containsKey(destName)) {
            throw new SimulationException("Destination building '" + destName + "' does not exist");
        }
        Building source = buildings.get(sourceName);
        Building dest = buildings.get(destName);

        // 把destbuilding 的 sourcelist当中 加上 sourcebuilding
        // if already exists, do nothing
        if (!dest.getSources().contains(sourceName)) {
            dest.addSource(sourceName);
        }

        roadMap.createPath(source, dest);
    }

    @Override
    public void printMap() {
        roadMap.printMap();
    }

    /**
     * 将 BasicSimulation 中部分属性转换为 React 前端可以读取的格式，输出 JSON 结构如下：
     * {
     *     currentTimeStep: int,
     *     verbosityLevel: int,
     *     buildings: String[],      // 每个元素为 Building.getName()
     *     recipes: String[],        // 每个元素为 Recipe.getName()
     *     roadMap: String[50][50]    // 根据 RoadMap 中的数据转化得到
     * }
     * @return JSONObject 包含上述字段
     */
    public JSONObject toSerializable() {
        JSONObject result = new JSONObject();

        // 直接复制两个数值字段
        result.put("currentTimeStep", this.currentTimeStep);
        result.put("verbosityLevel", this.verbosityLevel);

        // 将 buildings（Map<String, Building>）转换为名称数组
        List<String> buildingNames = new ArrayList<>();
        for (Building b : buildings.values()) {
            buildingNames.add(b.getName());
        }
        result.put("buildings", new JSONArray(buildingNames));

        // 将 recipes（Map<String, Recipe>）转换为名称数组
        List<String> recipeNames = new ArrayList<>();
        for (Recipe r : recipes.values()) {
            recipeNames.add(r.getName());
        }
        result.put("recipes", new JSONArray(recipeNames));

        // 初始化 50×50 的二维数组，默认值为 null
        Object[][] arr = new Object[50][50];

        // 处理建筑位置：
        // 如果坐标 (x, y) 存在建筑，则赋值为 JSONArray 包含两个字符串，
        // 第一个为 Building.getName(), 第二个为 Building.toString()
        for (Map.Entry<Coordinate, Building> entry : roadMap.getBuildingLocations().entrySet()) {
            Coordinate coord = entry.getKey();
            int x = coord.getX();
            int y = coord.getY();
            if (x >= 0 && x < 50 && y >= 0 && y < 50) {
                JSONArray buildingInfo = new JSONArray();
                buildingInfo.put(entry.getValue().getName());
                buildingInfo.put(entry.getValue().toString());
                arr[x][y] = buildingInfo;
            }
        }

        // 处理路信息：
        // 对于没有建筑的坐标，如果存在路数据，则构造对应的方向字符串，
        // 新规则：对于每个方向按顺序 [N, E, S, W]，
        // 如果该路格在该方向的 exitDirections 中，则字符为 "2"；否则，
        // 如果在 enterDirections 中，则字符为 "1"；否则为 "0"。
        for (Map.Entry<Coordinate, Road> entry : roadMap.getRoads().entrySet()) {
            Coordinate coord = entry.getKey();
            int x = coord.getX();
            int y = coord.getY();
            if (x >= 0 && x < 50 && y >= 0 && y < 50 && arr[x][y] == null) {
                Road road = entry.getValue();
                StringBuilder sb = new StringBuilder();
                // 按照 NORTH, EAST, SOUTH, WEST 顺序
                for (Direction d : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
                    if (road.getExitDirections().contains(d)) {
                        sb.append("2");
                    } else if (road.getEnterDirections().contains(d)) {
                        sb.append("1");
                    } else {
                        sb.append("0");
                    }
                }
                arr[x][y] = sb.toString();
            }
        }

        // 将二维数组封装成 JSON 数组
        JSONArray roadMapJson = new JSONArray();
        for (int i = 0; i < 50; i++) {
            JSONArray row = new JSONArray();
            for (int j = 0; j < 50; j++) {
                // 如果 arr[i][j] 为 null，则在 JSON 中为 null；否则添加对应对象
                row.put(arr[j][i]);
            }
            roadMapJson.put(row);
        }
        result.put("roadMap", roadMapJson);

        result.put("mapText", roadMap.printMap());

        List<String> typeNames = new ArrayList<>(this.buildableTypes.keySet());
        result.put("buildableTypes", new JSONArray(typeNames));

        return result;
    }

    public void setRoadMap(RoadMap rm) {
        roadMap = rm;
    }

    @Override
    public void simpleRemove(String src, String dest) {
        Building srcBuilding = buildings.get(src);
        Building destBuilding = buildings.get(dest);
        roadMap.simpleRemoval(srcBuilding, destBuilding);
    }

    @Override
    public void complexRemove(String src, String dest) {
        Building srcBuilding = buildings.get(src);
        Building destBuilding = buildings.get(dest);
        roadMap.complexRemoval(srcBuilding, destBuilding);
    }
    @Override
    public void pause() {
        timeRate = 0;
    }

    @Override
    public void setRate(int rate) {
        timeRate = rate;
    }

    @Override
    public int getRate() {
        return timeRate;
    }

}