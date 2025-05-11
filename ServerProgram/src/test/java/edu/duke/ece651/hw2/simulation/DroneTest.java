package edu.duke.ece651.hw2.simulation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DroneTest {

    // @Test
    // void testConstructionCompletesAfterTenSteps() {
    //     Drone d = new Drone();
    //     assertEquals(DroneStatus.IN_CONSTRUCTION, d.getStatus(), "新建时应为 IN_CONSTRUCTION");

    //     // 模拟 10 个建造步骤
    //     for (int i = 0; i < 10; i++) {
    //         d.step();
    //     }
    //     assertEquals(DroneStatus.IDLE, d.getStatus(), "建造完成后应为 IDLE");
    // }

    // @Test
    // void testWorkAssignmentAndCompletion() {
    //     Drone d = new Drone();
    //     // 先完成建造
    //     for (int i = 0; i < 10; i++) {
    //         d.step();
    //     }
    //     assertEquals(DroneStatus.IDLE, d.getStatus());

    //     // 指派 5 个工作周期
    //     d.assignWork(5);
    //     d.setStatus(DroneStatus.ACTIVE);
    //     assertEquals(DroneStatus.ACTIVE, d.getStatus(), "指派工作后应为 ACTIVE");

    //     // 模拟 5 个工作步骤
    //     for (int i = 0; i < 5; i++) {
    //         d.step();
    //     }
    //     assertEquals(DroneStatus.IDLE, d.getStatus(), "工作完成后应恢复为 IDLE");
    // }
}