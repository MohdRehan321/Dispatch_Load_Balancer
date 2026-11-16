package com.dispatch_load_balancer.service;

import com.dispatch_load_balancer.entity.DeliveryOrder;
import com.dispatch_load_balancer.entity.Vehicle;
import com.dispatch_load_balancer.enums.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DispatchPersistenceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private VehicleService vehicleService;

    @Test
    void ordersAndVehiclesCanBeReplacedAndReadBackFromPostgres() {
        DeliveryOrder order = new DeliveryOrder(
                null, "PHASE5-ORD-1", 28.6139, 77.2090,
                "Phase 5 test order", new BigDecimal("2.500"), Priority.HIGH
        );
        Vehicle vehicle = new Vehicle(
                null, "PHASE5-VEH-1", new BigDecimal("50.000"),
                28.6000, 77.2000, "Phase 5 test depot"
        );

        orderService.replaceOrders(List.of(order));
        vehicleService.replaceVehicles(List.of(vehicle));

        List<DeliveryOrder> savedOrders = orderService.getOrders();
        List<Vehicle> savedVehicles = vehicleService.getVehicles();

        assertTrue(savedOrders.stream()
                .anyMatch(saved -> saved.getOrderId().equals("PHASE5-ORD-1")));
        assertTrue(savedVehicles.stream()
                .anyMatch(saved -> saved.getVehicleId().equals("PHASE5-VEH-1")));

        DeliveryOrder savedOrder = savedOrders.stream()
                .filter(saved -> saved.getOrderId().equals("PHASE5-ORD-1"))
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("2.500"), savedOrder.getPackageWeight());
        assertEquals(Priority.HIGH, savedOrder.getPriority());

        Vehicle savedVehicle = savedVehicles.stream()
                .filter(saved -> saved.getVehicleId().equals("PHASE5-VEH-1"))
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("50.000"), savedVehicle.getCapacity());
    }
}
