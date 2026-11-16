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

    @Test
    void heavierOrderIsConsideredFirstWithinSamePriority() {
        DeliveryOrder lighter = order(
                "ORD-A", 4, Priority.HIGH, 28.61, 77.20
        );
        DeliveryOrder heavier = order(
                "ORD-B", 6, Priority.HIGH, 28.62, 77.21
        );

        OptimizationResult result = optimizer.optimize(
                List.of(lighter, heavier),
                List.of(vehicle("VEH001", 6))
        );

        assertEquals("ORD-B", result.routes().get(0).orders().get(0).getOrderId());
        assertEquals("ORD-A", result.unassignedOrders().get(0).order().getOrderId());
    }

    @Test
    void orderExactlyEqualToVehicleCapacityCanBeAssigned() {
        DeliveryOrder order = order(
                "ORD-EXACT", 25, Priority.MEDIUM, 28.61, 77.20
        );

        OptimizationResult result = optimizer.optimize(
                List.of(order),
                List.of(vehicle("VEH001", 25))
        );

        assertEquals(1, result.routes().get(0).orders().size());
        assertEquals(new BigDecimal("25.0"), result.routes().get(0).totalLoad());
        assertTrue(result.unassignedOrders().isEmpty());
    }

    @Test
    void unassignedOrderReportsFleetCapacityExhaustedWhenCapacityIsUsed() {
        List<DeliveryOrder> orders = List.of(
                order("ORD-1", 7, Priority.HIGH, 28.61, 77.20),
                order("ORD-2", 7, Priority.LOW, 28.62, 77.21)
        );

        OptimizationResult result = optimizer.optimize(
                orders, List.of(vehicle("VEH001", 10))
        );

        assertEquals(1, result.routes().get(0).orders().size());
        assertEquals(1, result.unassignedOrders().size());
        assertEquals(
                UnassignedReason.FLEET_CAPACITY_EXHAUSTED,
                result.unassignedOrders().get(0).reason()
        );
    }

    @Test
    void allOrdersAreUnassignedWithNoVehicles() {
        DeliveryOrder order = order(
                "ORD-1", 5, Priority.HIGH, 28.61, 77.20
        );

        OptimizationResult result = optimizer.optimize(List.of(order), List.of());

        assertTrue(result.routes().isEmpty());
        assertEquals(1, result.unassignedOrders().size());
        assertEquals(
                UnassignedReason.NO_FEASIBLE_ASSIGNMENT,
                result.unassignedOrders().get(0).reason()
        );
    }

    @Test
    void vehicleRoutesAreReturnedInVehicleIdOrder() {
        OptimizationResult result = optimizer.optimize(
                List.of(),
                List.of(vehicle("VEH-Z", 10), vehicle("VEH-A", 10))
        );

        assertEquals("VEH-A", result.routes().get(0).vehicle().getVehicleId());
        assertEquals("VEH-Z", result.routes().get(1).vehicle().getVehicleId());
    }

    @Test
    void routeDistanceIncludesDepotToEachStopButNoReturnToDepot() {
        Vehicle depot = vehicle("VEH001", 20);
        DeliveryOrder first = order(
                "ORD-1", 5, Priority.HIGH, 28.6139, 77.2090
        );
        DeliveryOrder second = order(
                "ORD-2", 5, Priority.LOW, 28.5355, 77.3910
        );

        OptimizationResult result = optimizer.optimize(
                List.of(first, second), List.of(depot)
        );

        double expected = new HaversineDistanceCalculator().calculate(
                depot.getCurrentLatitude(), depot.getCurrentLongitude(),
                first.getLatitude(), first.getLongitude()
        ) + new HaversineDistanceCalculator().calculate(
                first.getLatitude(), first.getLongitude(),
                second.getLatitude(), second.getLongitude()
        );

        assertEquals(expected, result.routes().get(0).totalDistanceKm(), 0.000001);
    }


    @Test
    void twoOptImprovesTheGreedyRouteWhenAReversalReducesDistance() {
        Vehicle depot = vehicle("VEH001", 100);
        List<DeliveryOrder> orders = List.of(
                order("ORD-1", 1, Priority.HIGH, 28.6632843894, 77.3243818917),
                order("ORD-2", 1, Priority.HIGH, 28.6974859164, 77.3168379495),
                order("ORD-3", 1, Priority.HIGH, 28.6595772043, 77.3893875870),
                order("ORD-4", 1, Priority.HIGH, 28.6790134863, 77.3998090870),
                order("ORD-5", 1, Priority.HIGH, 28.6679376137, 77.2276208128)
        );

        OptimizationResult result = optimizer.optimize(orders, List.of(depot));

        List<String> optimizedOrderIds = result.routes().get(0).orders().stream()
                .map(DeliveryOrder::getOrderId)
                .toList();

        double greedyRouteDistanceBeforeTwoOpt = 30.2319;
        assertEquals(
                List.of("ORD-5", "ORD-2", "ORD-1", "ORD-3", "ORD-4"),
                optimizedOrderIds
        );
        assertTrue(
                result.routes().get(0).totalDistanceKm() < greedyRouteDistanceBeforeTwoOpt,
                "2-opt should reduce the greedy route distance"
        );
    }

}
