package edu.duke.ece651.hw2.simulation;

public class Coordinate {
    private int x;
    private int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public int manhattanDistance(Coordinate other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    public Coordinate getNeighbor(Direction direction) {
        switch(direction) {
            case NORTH: return new Coordinate(x, y - 1);
            case EAST:  return new Coordinate(x + 1, y);
            case SOUTH: return new Coordinate(x, y + 1);
            case WEST:  return new Coordinate(x - 1, y);
            default:    return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate)) return false;
        Coordinate c = (Coordinate) o;
        return this.x == c.x && this.y == c.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}