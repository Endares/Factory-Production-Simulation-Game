package edu.duke.ece651.hw2.simulation;

public class Drone {
    private DroneStatus status;
    // how many steps remained to work
    private int workSteps;
    private int constructSteps;
    public Drone() {
        status = DroneStatus.IN_CONSTRUCTION;
        workSteps = 0;
        constructSteps = 10;
    }
    public DroneStatus getStatus() {
        return this.status;
    }
    public void assignWork(int steps) {
        workSteps = steps;
    }
    // work one step
    public void doWork() {
        --workSteps;
        if (workSteps == 0) {
            this.setStatus(DroneStatus.IDLE);
        }
    }
    public void setStatus(DroneStatus s) {
        this.status = s;
    }
    public void doConstruction() {
        --constructSteps;
        if (constructSteps == 0) {
            this.setStatus(DroneStatus.IDLE);
        }
    }
    public void step() {
        if (this.status == DroneStatus.ACTIVE) {
            doWork();
        } else if (this.status == DroneStatus.IN_CONSTRUCTION) {
            doConstruction();
        }
    }
}
