package edu.duke.ece651.hw2.simulation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for StorageBuilding class.
 */
public class StorageBuildingTest {

    @Test
    public void testGetProvidedOutputs() {
        StorageBuilding sb = new StorageBuilding("Bolt Storage", "bolt", 100, 1.7f, Arrays.asList("Factory1", "Factory2"));
        List<String> expected = Collections.singletonList("bolt");
        // getProvidedOutputs 返回的列表必须只包含 storedItem，并且每次返回的新列表不能是同一引用
        assertEquals(expected, sb.getProvidedOutputs());
        List<String> outputs1 = sb.getProvidedOutputs();
        List<String> outputs2 = sb.getProvidedOutputs();
        assertNotSame(outputs1, outputs2, "getProvidedOutputs should return a new list each time");
    }

    @Test
    public void testCanProduce() {
        StorageBuilding sb = new StorageBuilding("Bolt Storage", "bolt", 100, 1.7f, List.of("Factory1"));
        // 只有 storedItem 正确时返回 true，否则返回 false
        assertTrue(sb.canProduce("bolt"), "StorageBuilding should produce its stored item");
        assertFalse(sb.canProduce("wood"), "StorageBuilding should not produce items different from stored item");
    }

    @Test
    public void testDeliverItemUpdatesStorage() {
        StorageBuilding sb = new StorageBuilding("Bolt Storage", "bolt", 100, 1.7f, Arrays.asList("Factory1", "Factory2"));
        assertEquals(0, sb.getCurrentStorage(), "Initial storage should be 0");
        sb.deliverItem("bolt", 20);
        assertEquals(20, sb.getCurrentStorage(), "After delivering 20 bolts, storage should be 20");
        sb.deliverItem("bolt", 15);
        assertEquals(35, sb.getCurrentStorage(), "After delivering additional 15 bolts, storage should be 35");
    }

    @Test
    public void testDeliverItemForDifferentItemDoesNotAffectStorage() {
        StorageBuilding sb = new StorageBuilding("Bolt Storage", "bolt", 100, 1.7f, List.of("Factory1"));
        sb.deliverItem("wood", 50);
        // deliverItem 不会影响存储项外的物品
        assertEquals(0, sb.getCurrentStorage(), "Delivering non-stored item should not update storage");
    }

    @Test
    public void testAddRequestIncreasesQueueLength() {
        StorageBuilding sb = new StorageBuilding("Bolt Storage", "bolt", 100, 1.7f, List.of("Factory1"));
        // 初始队列为空
        assertEquals(0, sb.getQueueLength(), "Initial request queue should be empty");
        Request r1 = new Request(1, new Recipe("bolt", new HashMap<>(), 1), sb, true, 0);
        sb.addRequest(r1);
        assertEquals(1, sb.getQueueLength(), "Queue length should be 1 after adding one request");
        Request r2 = new Request(2, new Recipe("bolt", new HashMap<>(), 1), sb, false, 0);
        sb.addRequest(r2);
        assertEquals(2, sb.getQueueLength(), "Queue length should be 2 after adding two requests");
    }

    @Test
    public void testToStringContainsImportantInfo() {
        StorageBuilding sb = new StorageBuilding("Bolt Storage", "bolt", 100, 1.7f, Arrays.asList("Factory1", "Factory2", "Factory3"));
        sb.deliverItem("bolt", 40);
        String s = sb.toString();
        // 检查名称、类型、存储项、容量、优先级及 outstandingRequests（初始为 0）
        assertTrue(s.contains("Bolt Storage"), "toString should contain building name");
        assertTrue(s.contains("Storage"), "toString should indicate Storage type");
        assertTrue(s.contains("bolt"), "toString should contain stored item");
        assertTrue(s.contains("100"), "toString should contain capacity");
        assertTrue(s.contains("1.7"), "toString should contain priority");
        assertTrue(s.contains("OutstandingRequests: 0"), "toString should show outstanding requests as 0");
        assertTrue(s.contains("Storage:"), "toString should have Storage section");
    }

    @Test
    public void testMultipleDeliveries() {
        StorageBuilding sb = new StorageBuilding("Bolt Storage", "bolt", 200, 2.0f, Arrays.asList("Factory1", "Factory2"));
        sb.deliverItem("bolt", 10);
        sb.deliverItem("bolt", 20);
        sb.deliverItem("bolt", 30);
        assertEquals(60, sb.getCurrentStorage(), "Cumulative delivered bolts should be 60");
    }

    @Test
    public void testGetSourcesIndependence() {
        StorageBuilding sb = new StorageBuilding("Bolt Storage", "bolt", 100, 1.7f, Arrays.asList("Factory1", "Factory2"));
        List<String> s1 = sb.getSources();
        List<String> s2 = sb.getSources();
        assertNotSame(s1, s2, "getSources should return a new list instance each time");
        assertEquals(s1, s2, "The content of sources list should be equal");
    }

    @Test
    public void testCoordinateSetterGetter() {
        StorageBuilding sb = new StorageBuilding("Bolt Storage", "bolt", 100, 1.7f, List.of("Factory1"));
        Coordinate coord = new Coordinate(10, 20);
        sb.setLocation(coord);
        assertEquals(coord, sb.getLocation(), "Coordinate should be correctly stored and retrieved");
    }

    @Test
    public void testOutstandingRequestsInToString() {
        // 通过 toString 检查 outstandingRequests 的初始显示
        StorageBuilding sb = new StorageBuilding("Bolt Storage", "bolt", 150, 2.5f, List.of("FactoryA"));
        String output = sb.toString();
        assertTrue(output.contains("OutstandingRequests: 0"), "Initially, outstanding requests should be 0");
    }

    @Test
    public void testToStringFormatAfterMultipleOperations() {
        // 测试经过多次 deliverItem 和添加 request 后，toString 输出包含全部更新信息
        StorageBuilding sb = new StorageBuilding("Bolt Storage", "bolt", 120, 1.9f, Arrays.asList("FactoryX", "FactoryY"));
        sb.deliverItem("bolt", 35);
        sb.addRequest(new Request(1, new Recipe("bolt", new HashMap<>(), 1), sb, false, 0));
        sb.addRequest(new Request(2, new Recipe("bolt", new HashMap<>(), 1), sb, false, 0));
        String s = sb.toString();
        assertTrue(s.contains("Bolt Storage"), "toString should contain building name");
        assertTrue(s.contains("bolt"), "toString should contain stored item");
        assertTrue(s.contains("120"), "toString should contain capacity 120");
        assertTrue(s.contains("1.9"), "toString should contain priority 1.9");
        // Check that request queue information is printed
        assertTrue(s.contains("RequestQueue:"), "toString should include request queue info");
    }
}