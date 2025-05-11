package edu.duke.ece651.hw2.simulation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DroneBuildingTest {

    @SuppressWarnings("unchecked")
    private List<Drone> getDroneList(DroneBuilding b) throws Exception {
        Field f = DroneBuilding.class.getDeclaredField("droneList");
        f.setAccessible(true);
        return (List<Drone>) f.get(b);
    }

    @Test
    void testConstructDroneAndCanProduce() throws Exception {
        DroneBuilding port = new DroneBuilding("Port1");
        assertEquals(0, port.getDroneNumber(), "初始时应无无人机");
        assertFalse(port.canProduce("anything"), "只能生产 'drone'");

        port.constructDrone();
        assertEquals(1, port.getDroneNumber(), "构造后应有 1 架无人机");
        assertTrue(port.canProduce("drone"), "canProduce(\"drone\") 应返回 true");
    }

    @Test
    void testAutomaticConstructionOnStep() throws Exception {
        DroneBuilding port = new DroneBuilding("Port1");
        // 第一次调用 step(0) 会自动构造一架
        port.step(0, 0);
        assertEquals(1, port.getDroneNumber());
        // 目前还正在建造，没有IDLE的无人机
        assertFalse(port.hasDrone());

        // 再次在 timeStep=10 时触发
        port.step(10, 0);
        assertEquals(2, port.getDroneNumber());
    }

    @Test
    void testUseDroneAndHasDrone() throws Exception {
        DroneBuilding port = new DroneBuilding("Port1");
        port.constructDrone();
        List<Drone> drones = getDroneList(port);
        Drone d = drones.get(0);

        // 完成建造，使其变为 IDLE
        for (int i = 0; i < 10; i++) {
            d.doConstruction();
        }
        assertEquals(DroneStatus.IDLE, d.getStatus());

        // 此时 should have an idle drone
        assertTrue(port.hasDrone(), "有空闲无人机时 hasDrone 应返回 true");

        // 指派 3 步工作周期
        port.useDrone(3);
        assertEquals(DroneStatus.ACTIVE, d.getStatus(), "指派后无人机应为 ACTIVE");

        // 模拟 3 个工作步骤
        for (int i = 0; i < 3; i++) {
            d.doWork();
        }
        assertEquals(DroneStatus.IDLE, d.getStatus(), "工作完成后应变回 IDLE");
    }
}