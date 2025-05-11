package edu.duke.ece651.hw2.simulation;

import java.util.ArrayList;
import java.util.List;

public class DroneBuilding extends BasicBuilding {
    private List<Drone> droneList;
    // if has any available(idle) drones

    /*
     * Construct a drone building
     * 
     */
    public DroneBuilding(String name, List<String> sources) {
        super(name, sources);
        droneList = new ArrayList<>();
    }


    public boolean hasDrone() {
        return (countIdleDrones() > 0);
    }
    // use a drone for time steps
    public void useDrone(int time) {
        if (!hasDrone()) {
            System.out.println("No drones available!");
            return;
        }
        for (Drone d : droneList) {
            if (d.getStatus() == DroneStatus.IDLE) {
                d.assignWork(time);
                d.setStatus(DroneStatus.ACTIVE);
                break;
            }
        }
    }

    public DroneBuilding(String name) {
        super(name, new ArrayList<>());
        droneList = new ArrayList<>();
    }

    // get the total number of drones in the droneList
    public int getDroneNumber() {
        return droneList.size();
    }

    public int countIdleDrones() {
        int count = 0;
        for (Drone d : droneList) {
            if (d.getStatus() == DroneStatus.IDLE) {
                ++count;
            }
        }
        return count;
    }

    public int countActiveDrones() {
        int count = 0;
        for (Drone d : droneList) {
            if (d.getStatus() == DroneStatus.ACTIVE) {
                ++count;
            }
        }
        return count;
    }

    public int countInConstructDrones() {
        int count = 0;
        for (Drone d : droneList) {
            if (d.getStatus() == DroneStatus.IN_CONSTRUCTION) {
                ++count;
            }
        }
        return count;
    }

    // construct a new drone
    public void constructDrone() {
        if (droneList.size() >= 10) {
            System.out.println("Cannot make any new drones!");
            return;
        }
        Drone d = new Drone();
        droneList.add(d);
    }

    // will return an empty list
    @Override
    public List<Request> step(int currentTimeStep, int verbosity) {
        if (currentTimeStep % 10 == 0 && droneList.size() < 10) {
            constructDrone();
        }
        for (Drone d : droneList) {
            d.step();
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getProvidedOutputs() {
        return new ArrayList<>();
    }

    @Override
    public boolean canProduce(String itemName) {
        return itemName == "drone";
    }

    @Override
    public String toString() {
        return super.toString() + "DronePort: " + this.name + "\n" +
                "Idle: " + countIdleDrones() + "\n" + 
                "Active: " + countActiveDrones() + "\n" + 
                "InConstruction: " + countInConstructDrones() + "\n";
    }
}
