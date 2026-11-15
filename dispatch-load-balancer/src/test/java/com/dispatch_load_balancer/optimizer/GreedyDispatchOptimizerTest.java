package com.dispatch_load_balancer.optimizer;

import com.dispatch_load_balancer.entity.DeliveryOrder;
import com.dispatch_load_balancer.entity.Vehicle;
import com.dispatch_load_balancer.enums.Priority;
import com.dispatch_load_balancer.geo.HaversineDistanceCalculator;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GreedyDispatchOptimizerTest {

    private final GreedyDispatchOptimizer optimizer =
            new GreedyDispatchOptimizer(new HaversineDistanceCalculator());

    private DeliveryOrder order(
            String id, double weight, Priority priority,
            double latitude, double longitude) {

        DeliveryOrder order = mock(DeliveryOrder.class);

        when(order.getOrderId()).thenReturn(id);
        when(order.getPackageWeight())
                .thenReturn(BigDecimal.valueOf(weight));
        when(order.getPriority()).thenReturn(priority);
        when(order.getLatitude()).thenReturn(latitude);
        when(order.getLongitude()).thenReturn(longitude);
        when(order.getAddress()).thenReturn("Address " + id);

        return order;
    }

    private Vehicle vehicle(String id, double capacity) {

        Vehicle vehicle = mock(Vehicle.class);

        when(vehicle.getVehicleId()).thenReturn(id);
        when(vehicle.getCapacity())
                .thenReturn(BigDecimal.valueOf(capacity));
        when(vehicle.getCurrentLatitude()).thenReturn(28.6139);
        when(vehicle.getCurrentLongitude()).thenReturn(77.2090);
        when(vehicle.getCurrentAddress()).thenReturn("Delhi");

        return vehicle;
    }

    @Test
    void highPriorityOrderShouldBeAssignedBeforeLowPriorityOrder() {

        DeliveryOrder high = order(
                "ORD-HIGH", 10, Priority.HIGH, 28.61, 77.20
        );

        DeliveryOrder low = order(
                "ORD-LOW", 10, Priority.LOW, 28.62, 77.21
        );

        OptimizationResult result = optimizer.optimize(
                List.of(low, high),
                List.of(vehicle("VEH001", 10))
        );

        assertEquals(1, result.routes().get(0).orders().size());
        assertEquals("ORD-HIGH",
                result.routes().get(0).orders().get(0).getOrderId());

        assertEquals(1, result.unassignedOrders().size());
        assertEquals("ORD-LOW",
                result.unassignedOrders().get(0).order().getOrderId());
    }

    @Test
    void orderHeavierThanEveryVehicleShouldBeUnassigned() {

        DeliveryOrder heavy = order(
                "ORD001", 150, Priority.HIGH, 28.61, 77.20
        );

        OptimizationResult result = optimizer.optimize(
                List.of(heavy),
                List.of(vehicle("VEH001", 100))
        );

        assertEquals(1, result.unassignedOrders().size());
        assertEquals(
                UnassignedReason.EXCEEDS_MAX_VEHICLE_CAPACITY,
                result.unassignedOrders().get(0).reason()
        );
    }

    @Test
    void assignedOrdersMustNeverExceedVehicleCapacity() {

        List<DeliveryOrder> orders = List.of(
                order("ORD001", 40, Priority.HIGH, 28.61, 77.20),
                order("ORD002", 30, Priority.MEDIUM, 28.62, 77.21),
                order("ORD003", 20, Priority.LOW, 28.63, 77.22)
        );

        OptimizationResult result = optimizer.optimize(
                orders,
                List.of(
                        vehicle("VEH001", 50),
                        vehicle("VEH002", 50)
                )
        );

        for (VehicleRoute route : result.routes()) {
            assertTrue(
                    route.totalLoad().compareTo(
                            route.vehicle().getCapacity()
                    ) <= 0
            );
        }
    }

    @Test
    void everyOrderMustBeAssignedOrUnassignedExactlyOnce() {

        List<DeliveryOrder> orders = List.of(
                order("ORD001", 20, Priority.HIGH, 28.61, 77.20),
                order("ORD002", 20, Priority.MEDIUM, 28.62, 77.21),
                order("ORD003", 20, Priority.LOW, 28.63, 77.22)
        );

        OptimizationResult result = optimizer.optimize(
                orders,
                List.of(vehicle("VEH001", 40))
        );

        int assigned = result.routes().stream()
                .mapToInt(route -> route.orders().size())
                .sum();

        assertEquals(
                orders.size(),
                assigned + result.unassignedOrders().size()
        );
    }

    @Test
    void emptyOrdersShouldProduceEmptyRoutes() {

        OptimizationResult result = optimizer.optimize(
                List.of(),
                List.of(vehicle("VEH001", 100))
        );

        assertEquals(1, result.routes().size());
        assertTrue(result.routes().get(0).orders().isEmpty());
        assertEquals(BigDecimal.ZERO, result.routes().get(0).totalLoad());
        assertEquals(0.0, result.routes().get(0).totalDistanceKm());
    }
}

