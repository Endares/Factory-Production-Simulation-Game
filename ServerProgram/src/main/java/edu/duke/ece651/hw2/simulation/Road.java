package edu.duke.ece651.hw2.simulation;

import java.util.ArrayList;
import java.util.List;

public class Road {
    private Coordinate location;
    private int sharedCount;
    private List<Direction> enterDirections;
    private List<Direction> exitDirections;

    public Road(Coordinate location) {
        this.location = location;
        this.sharedCount = 0;
        this.enterDirections = new ArrayList<>();
        this.exitDirections = new ArrayList<>();
    }

    public Coordinate getLocation() {
        return location;
    }

    public void addEnterDirection(Direction dir) {
        if (!enterDirections.contains(dir)) {
            enterDirections.add(dir);
        }
    }

    public void addExitDirection(Direction dir) {
        if (!exitDirections.contains(dir)) {
            exitDirections.add(dir);
        }
    }

    public List<Direction> getEnterDirections() {
        return new ArrayList<>(enterDirections);
    }

    public List<Direction> getExitDirections() {
        return new ArrayList<>(exitDirections);
    }

    public void increSharedCountBy(int n) {
        sharedCount += n;
    }

    public int getSharedCount() {
        return sharedCount;
    }
}