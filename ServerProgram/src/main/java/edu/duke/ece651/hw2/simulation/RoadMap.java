package edu.duke.ece651.hw2.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

class Triple<F, S, T> {
    private final F first;
    private final S second;
    private final T third;

    public Triple(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public F getFirst() { return first; }
    public S getSecond() { return second; }
    public T getThird() { return third; }
}

public class RoadMap {
    // 已存在的路格信息：key 为路格坐标，value 为 Road 对象
    private Map<Coordinate, Road> roads;
    // 建筑位置映射（非路格）
    private Map<Coordinate, Building> buildingLocations;
    // all pairs of connections，方向敏感，去重
    private List<Pair<Building, Building>> connectionSet = new ArrayList<>();

    public Map<Coordinate, Road> getRoads() {
        return roads;
    }

    public Map<Coordinate, Building> getBuildingLocations() {
        return buildingLocations;
    }

    public RoadMap() {
        roads = new HashMap<>();
        buildingLocations = new HashMap<>();
    }

    public void addBuilding(Building building) {
        if (building.getLocation() != null) {
            buildingLocations.put(building.getLocation(), building);
        }
    }

    // 辅助方法：根据两点坐标获得移动方向（仅支持相邻格）
    private Direction getDirection(Coordinate from, Coordinate to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        if (dx == 1 && dy == 0) return Direction.EAST;
        if (dx == -1 && dy == 0) return Direction.WEST;
        if (dx == 0 && dy == 1) return Direction.SOUTH;
        if (dx == 0 && dy == -1) return Direction.NORTH;
        throw new IllegalArgumentException("无法判断 " + from + " 到 " + to + " 的方向");
    }

    // 辅助方法：返回方向 d 的相反方向
    private Direction getOpposite(Direction d) {
        return switch (d) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
        };
    }

    // 内部辅助类，表示搜索状态
    private class Node {
        Coordinate coord;   // 当前所在格
        int totalCost;      // 累计总代价 = 步数 + 新建格数
        int steps;          // 经过的步数
        Direction lastDir;  // 进入当前格使用的方向
        List<Coordinate> path;  // 从起点到当前状态的路径（不包含起点）

        Node(Coordinate coord, int totalCost, int steps, Direction lastDir, List<Coordinate> path) {
            this.coord = coord;
            this.totalCost = totalCost;
            this.steps = steps;
            this.lastDir = lastDir;
            this.path = path;
        }

        // 用于标识状态（坐标+最后移动方向）
        String key() {
            return coord.toString() + "_" + (lastDir == null ? "null" : lastDir.name());
        }
    }

    /**
     * 利用 Dijkstra 算法寻找一条从 source 到 dest 的最优路径。
     * 路径仅由网格坐标组成，不包含起点（建筑坐标），终点为达到目标建筑相邻的格。
     * 代价定义：
     *   每一步固定代价 1（表示路径长度），
     *   如果走向的格子需要新建（即 roads 中不存在该格），额外加 1。
     *   总代价 = 步数 + 新建格子数。
     *
     * @param source 起点建筑
     * @param dest   目标建筑
     * @return 从 source 到 dest 之间（连接建筑的）的最优路径（不含起点），若无路径则返回空列表。
     */
    public List<Coordinate> getOptimalPath(Building source, Building dest) {
        Coordinate start = source.getLocation();
        Coordinate goal = dest.getLocation();
        if (start == null || goal == null) return Collections.emptyList();

        List<Node> startNodes = new ArrayList<>();
        for (Direction d : Direction.values()) {
            Coordinate candidate = start.getNeighbor(d);
            if (candidate.equals(goal)) {
                List<Coordinate> directPath = new ArrayList<>();
                directPath.add(candidate);
                return directPath;
            }
            int stepCost = 1 + (roads.containsKey(candidate) ? 0 : 1);
            List<Coordinate> initPath = new ArrayList<>();
            initPath.add(candidate);
            startNodes.add(new Node(candidate, stepCost, 1, d, initPath));
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.totalCost));
        Map<String, Integer> bestCost = new HashMap<>();
        for (Node node : startNodes) {
            pq.offer(node);
            bestCost.put(node.key(), node.totalCost);
        }

        Node bestNode = null;
        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            if (cur.coord.manhattanDistance(goal) == 1) {
                bestNode = cur;
                break;
            }

            for (Direction d : Direction.values()) {
                Coordinate next = cur.coord.getNeighbor(d);

                if (next.getX() > 50 || next.getY() > 50 || next.getX() < 0 || next.getY() < 0) {
                    continue;
                }

                Road currentRoad = roads.get(cur.coord);
                Road nextRoad = roads.get(next);

                if (buildingLocations.containsKey(next)) {
                    continue;
                }

                // 如果当前路格存在但方向在已有出口中，允许；否则，如果是明确拒绝的方向，跳过
                if (currentRoad != null && !currentRoad.getExitDirections().isEmpty() && !currentRoad.getExitDirections().contains(d)) {
                    continue;
                }

                // 如果 next 路格存在且该方向被排除作为入口，则跳过
                if (nextRoad != null && !nextRoad.getEnterDirections().isEmpty() && !nextRoad.getEnterDirections().contains(getOpposite(d))) {
                    continue;
                }

                int stepCost = 1 + (roads.containsKey(next) ? 0 : 1);
                int newCost = cur.totalCost + stepCost;
                List<Coordinate> newPath = new ArrayList<>(cur.path);
                newPath.add(next);
                Node nextNode = new Node(next, newCost, cur.steps + 1, d, newPath);
                String key = nextNode.key();
                if (!bestCost.containsKey(key) || newCost < bestCost.get(key)) {
                    bestCost.put(key, newCost);
                    pq.offer(nextNode);
                }
            }
        }

        if (bestNode == null) return Collections.emptyList();

        // 在路径确定后，更新路径上的方向信息（如有必要）
        Coordinate prev = start;
        for (Coordinate coord : bestNode.path) {
            Direction d = getDirection(prev, coord);

            Road road = roads.computeIfAbsent(coord, Road::new);
            if (!road.getEnterDirections().contains(getOpposite(d))) {
                road.addEnterDirection(getOpposite(d));
            }

            Road prevRoad = roads.computeIfAbsent(prev, Road::new);
            if (!prevRoad.getExitDirections().contains(d)) {
                prevRoad.addExitDirection(d);
            }

            prev = coord;
        }

        return bestNode.path;
    }

    /**
     * 根据计算得到的最优路径，创建从 source 到 dest 的路径，
     * 对于路径中不存在的路格，新建 Road 对象，并设置出口方向。
     *
     * @param source 起点建筑
     * @param dest   目标建筑
     */
    public void createPath(Building source, Building dest) {
        List<Coordinate> path = getOptimalPath(source, dest);
        if (path.isEmpty()) {
            System.err.println("Can not create a path from " + source.getName() + " to " + dest.getName());
            return;
        }
        Coordinate prev = source.getLocation();
        for (Coordinate coord : path) {
            Direction d = getDirection(prev, coord);
            // 如果该路格不存在，则新建并设置出口方向
            if (!roads.containsKey(coord)) {
                Road newRoad = new Road(coord);
                newRoad.addExitDirection(d);
                // 同时，新建的路格默认设置入口方向为本次移动方向的相反方向
                newRoad.addEnterDirection(getOpposite(d));
                roads.put(coord, newRoad);
            }
            // 如果已存在，不修改已有方向（保证不反向）
            prev = coord;
        }
        
        // 如果是新的连接，sharedCount + 1， 代表一条新的路共享了当前格子
        if (!connectionSet.contains(new Pair<>(source, dest))) {
            for (Coordinate coord : path) {
                roads.get(coord).increSharedCountBy(1);
            }
            connectionSet.add(new Pair<>(source, dest));
        }
    }

    /**
     * goTime: 从 drone port 到 src 的时间 + src 到 dest 的时间
     * returnTime: goTime + dest 返回 port 的时间，即无人机离开的总时间
     */
    private Triple<DroneBuilding,Integer,Integer> getDroneDist(Coordinate src, Coordinate dest) {
        for (Building b : buildingLocations.values()) {
            if (b instanceof DroneBuilding db && db.hasDrone()) {
                Coordinate home = db.getLocation();
                // 距离 home→src 和 home→dest
                int d1 = home.manhattanDistance(src);
                int d2 = src.manhattanDistance(dest);
                int d3 = dest.manhattanDistance(home);
                if (d1 <= 20 && d3 <= 20) {
                    int goTime = d1 + d2;
                    int returnTime = goTime + d3;
                    return new Triple<>(db, goTime, returnTime);
                }
            }
        }
        return new Triple<>(null, -1, -1);
    }

    /**
     * 使用 BFS 求两建筑之间的最短距离，距离定义如下：
     * - 如果两建筑相邻，则距离为 0；
     * - 否则，距离为从 source 建筑到 dest 建筑所经过的路格步数加 1（即建筑与相邻路格连接的开销）。
     * 如果无法从 source 到 dest，则返回 -1。
     *
     * @param source 源建筑
     * @param dest   目标建筑
     * @return 两建筑之间的最短距离，或 -1 表示无法到达。
     */
    public int getShortestDistance(Building source, Building dest) {
        Coordinate sourceCoord = source.getLocation();
        Coordinate destCoord = dest.getLocation();
        if (sourceCoord == null || destCoord == null) {
            return -1;
        }

        // 用于存放最终的最短距离
        int bfsDist = -1;

        // 如果两建筑直接相邻，距离为 0（直接搬运，无需路格）
        if (sourceCoord.manhattanDistance(destCoord) == 1) {
            bfsDist = 0;
        } else {
            // 否则进行 BFS
            Queue<Coordinate> queue = new LinkedList<>();
            Map<Coordinate, Integer> distMap = new HashMap<>();

            // 将与 source 相邻的所有路格入队，距离记为 1
            for (Direction d : Direction.values()) {
                Coordinate adj = sourceCoord.getNeighbor(d);
                if (roads.containsKey(adj)) {
                    queue.offer(adj);
                    distMap.put(adj, 1);
                }
            }

            // BFS 循环
            while (!queue.isEmpty()) {
                Coordinate cur = queue.poll();
                int curDist = distMap.get(cur);

                // 如果该路格与 dest 相邻，则最终距离 = curDist + 1
                if (cur.manhattanDistance(destCoord) == 1) {
                    bfsDist = curDist + 1;
                    break;
                }

                Road currentRoad = roads.get(cur);
                if (currentRoad == null) {
                    continue;
                }

                // 按出口方向扩展
                for (Direction exitDir : currentRoad.getExitDirections()) {
                    Coordinate next = cur.getNeighbor(exitDir);
                    Road nextRoad = roads.get(next);
                    if (nextRoad == null) {
                        continue;
                    }
                    // 检查单向约束
                    if (!nextRoad.getEnterDirections().contains(getOpposite(exitDir))) {
                        continue;
                    }
                    if (!distMap.containsKey(next)) {
                        distMap.put(next, curDist + 1);
                        queue.offer(next);
                    }
                }
            }
        }

        // 尝试使用无人机
        Triple<DroneBuilding,Integer,Integer> droneInfo = getDroneDist(sourceCoord, destCoord);
        DroneBuilding db = droneInfo.getFirst();
        int goTime = droneInfo.getSecond();
        int returnTime = droneInfo.getThird();
        // System.out.println("goTime: " + goTime + " returnTime: " + returnTime);
        if (db != null && goTime >= 0 && (bfsDist == -1 || goTime < bfsDist)) {
            db.useDrone(returnTime);
            return goTime;
        }

        return bfsDist;
    }

    // 打印 RoadMap
    public String printMap() {
        StringBuilder sb = new StringBuilder();

        // 计算所有建筑和路格的坐标边界
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (Coordinate c : buildingLocations.keySet()) {
            minX = Math.min(minX, c.getX());
            maxX = Math.max(maxX, c.getX());
            minY = Math.min(minY, c.getY());
            maxY = Math.max(maxY, c.getY());
        }
        for (Coordinate c : roads.keySet()) {
            minX = Math.min(minX, c.getX());
            maxX = Math.max(maxX, c.getX());
            minY = Math.min(minY, c.getY());
            maxY = Math.max(maxY, c.getY());
        }

        // 打印上方的 x 轴坐标，预留左侧 4 个字符打印 y 轴坐标
        sb.append("Map:\n");
        sb.append("    ");  // 左侧留白
        for (int x = minX; x <= maxX; x++) {
            sb.append(String.format(" %2d ", x));
        }
        sb.append("\n");

        // 打印上边界
        sb.append("    ");
        sb.append("-----".repeat(Math.max(0, maxX - minX + 1)));
        sb.append("\n");

        // 从上到下打印地图，每行前面打印 y 轴坐标
        for (int y = minY; y <= maxY; y++) {
            // 打印 y 轴标签，宽度为 3，后跟一个竖线
            sb.append(String.format("%3d|", y));
            for (int x = minX; x <= maxX; x++) {
                Coordinate coord = new Coordinate(x, y);
                String cellContent;
                if (buildingLocations.containsKey(coord)) {
                    // 建筑：显示建筑名称（取前两个字符）
                    String name = buildingLocations.get(coord).getName();
                    cellContent = name.length() > 2 ? name.substring(0, 2) : name;
                } else if (roads.containsKey(coord)) {
                    Road road = roads.get(coord);
                    // 将出口和入口个数合并
                    Set<Direction> dirs = new HashSet<>(road.getExitDirections());
                    dirs.addAll(road.getEnterDirections());
                    int count = dirs.size();
                    if (count == 4) {
                        cellContent = "+";
                    } else if (count == 3) {
                        cellContent = "T";
                    } else if (count == 1) {
                        // 若为垂直方向
                        if (dirs.contains(Direction.NORTH) || dirs.contains(Direction.SOUTH)) {
                            cellContent = "l";
                        } else {
                            cellContent = "-";
                        }
                    } else if (count == 2) {
                        // 判断是否为直线或转角
                        if (dirs.contains(Direction.NORTH) && dirs.contains(Direction.SOUTH)) {
                            cellContent = "l";
                        } else if (dirs.contains(Direction.EAST) && dirs.contains(Direction.WEST)) {
                            cellContent = "-";
                        } else if (dirs.contains(Direction.EAST) && dirs.contains(Direction.NORTH)) {
                            cellContent = "L";
                        } else if (dirs.contains(Direction.EAST) && dirs.contains(Direction.SOUTH)) {
                            cellContent = "F";
                        } else if (dirs.contains(Direction.WEST) && dirs.contains(Direction.NORTH)) {
                            cellContent = "J";
                        } else {
                            cellContent = "7"; // 例如 West 和 South
                        }
                    } else {
                        cellContent = ".";
                    }
                } else {
                    cellContent = " ";  // 空白格子
                }
                // 每个格子固定宽度 2，左对齐，然后添加边界竖线
                sb.append(String.format(" %-2s|", cellContent));
            }
            sb.append("\n");
            // 打印每行的水平分割线
            sb.append("    ");
            sb.append("-----".repeat(Math.max(0, maxX - minX + 1)));
            sb.append("\n");
        }

        // 打印下方的 x 轴坐标（可选）
        sb.append("   ");
        for (int x = minX; x <= maxX; x++) {
            sb.append(String.format(" %2d ", x));
        }
        sb.append("\n");

        return sb.toString();
    }
    
    public void setRoads(Map<Coordinate, Road> roads) {
        this.roads = roads;
    }
    
    public void setBuildingLocations(Map<Coordinate, Building> buildingLocations) {
        this.buildingLocations = buildingLocations;
    }

    /**
     * 从地图中移除建筑
     * @param location 建筑位置
     */
    public void removeBuilding(Coordinate location) {
        buildingLocations.remove(location);
    }

    /*
     * 从地图当中移除路格
     * @param location 路格位置
     */
    public void removeRoad(Coordinate location) {
        roads.remove(location);
    }
    /**
     * 获取所有连接
     * @return 连接列表
     */
    public List<Pair<Building, Building>> getConnections() {
        return new ArrayList<>(connectionSet);
    }

    /**
     * Simple Removal: 
     * Remove all roads and rebuild the road network for all remaining connections.
     * @param src source building
     * @param dest destination building
     */
    public void simpleRemoval(Building src, Building dest) {
        if (!connectionSet.contains(new Pair<>(src, dest))) {
            System.out.println("Cannot remove non-existing path!");
            return;
        }
        connectionSet.remove(new Pair<>(src, dest));
        roads.clear();
        for (Pair<Building, Building> pair : connectionSet) {
            Building s = pair.first;
            Building d = pair.second;
            createPath(s, d);
        }
        System.out.println("Removed path from " + src.getName() + " to " + dest.getName());
        printConnections();
    }

    /**
     * Complex Removal: Remove only the roads that are no longer needed.
     * @param src source building
     * @param dest destination building
     */
    // public void complexRemoval(Building src, Building dest) {
    //     if (!connectionSet.contains(new Pair<>(src, dest))) {
    //         System.out.println("Cannot remove non-existing path!");
    //         return;
    //     }
    //     connectionSet.remove(new Pair<>(src, dest));
    //     List<Coordinate> path = getOptimalPath(src, dest);
    //     List<Coordinate> remain_path = path;
    //     for (Coordinate c : path) {
    //         Road rd = roads.get(c);
    //         rd.increSharedCountBy(-1);
    //         // find useless roads and delete
    //         if (rd.getSharedCount() <= 0) {
    //             roads.remove(c);
    //             remain_path.remove(c);
    //         }
    //     }
        
    //     // check if other roads really needs the remaining
    //     for (Coordinate c : remain_path) {
    //         boolean canDelete = true;
    //         for (Pair<Building, Building> pair : connectionSet) {
    //             List<Coordinate> path1 = getOptimalPath(pair.first, pair.second);
    //             for (Coordinate c1 : path1) {
    //                 if (c1.equals(c)) {
    //                     canDelete = false;
    //                     break;
    //                 }
    //             }
    //             if (!canDelete) break;
    //         }
    //         // no other connection uses this road
    //         if (canDelete) roads.remove(c);
    //     }
    //     System.out.println("Removed path from " + src.getName() + " to " + dest.getName());
    //     printConnections();
    // }

    public void complexRemoval(Building src, Building dest) {
        Pair<Building, Building> target = new Pair<>(src, dest);
        if (!connectionSet.contains(target)) {
            System.out.println("Cannot remove non-existing path!");
            return;
        }
    
        // 1. Remove the connection
        connectionSet.remove(target);
    
        // 2. Get the path and make a modifiable copy
        List<Coordinate> path = getOptimalPath(src, dest);
        List<Coordinate> remainPath = new ArrayList<>(path);
    
        // 3. Decrease shared count and remove roads with 0 count
        for (Coordinate c : path) {
            Road rd = roads.get(c);
            if (rd == null) continue;
            rd.increSharedCountBy(-1);
            if (rd.getSharedCount() <= 0) {
                roads.remove(c);
                remainPath.remove(c); // remove only if it has been deleted
            }
        }
    
        // 4. Build set of all coordinates still in use by remaining connections
        Set<Coordinate> usedCoords = new HashSet<>();
        for (Pair<Building, Building> pair : connectionSet) {
            List<Coordinate> path1 = getOptimalPath(pair.first, pair.second);
            usedCoords.addAll(path1);
        }
    
        // 5. Remove roads that are no longer needed
        for (Coordinate c : remainPath) {
            if (!usedCoords.contains(c)) {
                roads.remove(c);
            }
        }
    
        System.out.println("Removed path from " + src.getName() + " to " + dest.getName());
        printConnections();
    }

    public void printConnections() {
        for (Pair<Building, Building> pair : connectionSet) {
            System.out.println(pair.first.getName() + " to " + pair.second.getName());
        }
    }
}