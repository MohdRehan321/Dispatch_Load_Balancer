package com.dispatch_load_balancer.service;

import com.dispatch_load_balancer.dto.response.DispatchPlanResponse;
import com.dispatch_load_balancer.entity.DeliveryOrder;
import com.dispatch_load_balancer.entity.Vehicle;
import com.dispatch_load_balancer.enums.Priority;
import com.dispatch_load_balancer.exception.DispatchDataNotReadyException;
import com.dispatch_load_balancer.optimizer.DispatchOptimizer;
import com.dispatch_load_balancer.optimizer.OptimizationResult;
import com.dispatch_load_balancer.optimizer.UnassignedOrder;
import com.dispatch_load_balancer.optimizer.UnassignedReason;
import com.dispatch_load_balancer.optimizer.VehicleRoute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DispatchPlanServiceTest {

    private OrderService orderService;
    private VehicleService vehicleService;
    private DispatchOptimizer optimizer;
    private DispatchPlanService service;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        vehicleService = mock(VehicleService.class);
        optimizer = mock(DispatchOptimizer.class);
        service = new DispatchPlanService(orderService, vehicleService, optimizer);
    }

    @Test
    void generatePlanRejectsWhenNoOrdersAreUploaded() {
        when(orderService.getOrders()).thenReturn(List.of());
        when(vehicleService.getVehicles()).thenReturn(List.of());

        assertThrows(DispatchDataNotReadyException.class, service::generatePlan);
        verifyNoInteractions(optimizer);
    }

    @Test
    void generatePlanRejectsWhenNoVehiclesAreUploaded() {
        DeliveryOrder order = order("ORD-1", "5.000");
        when(orderService.getOrders()).thenReturn(List.of(order));
        when(vehicleService.getVehicles()).thenReturn(List.of());

        assertThrows(DispatchDataNotReadyException.class, service::generatePlan);
        verifyNoInteractions(optimizer);
    }

    @Test
    void generatePlanMapsAssignmentsUnassignedOrdersAndRoundedDistances() {
        DeliveryOrder assignedOrder = order("ORD-1", "5.000");
        DeliveryOrder unassignedOrder = order("ORD-2", "30.000");
        Vehicle vehicle = new Vehicle(
                null, "VEH-1", new BigDecimal("20.000"),
                28.61, 77.20, "Delhi Depot"
        );

        when(orderService.getOrders())
                .thenReturn(List.of(assignedOrder, unassignedOrder));
        when(vehicleService.getVehicles()).thenReturn(List.of(vehicle));

        VehicleRoute route = new VehicleRoute(
                vehicle, List.of(assignedOrder),
                new BigDecimal("5.000"), 10.5555
        );
        UnassignedOrder unassigned = new UnassignedOrder(
                unassignedOrder, UnassignedReason.EXCEEDS_MAX_VEHICLE_CAPACITY
        );
        when(optimizer.optimize(anyList(), anyList()))
                .thenReturn(new OptimizationResult(
                        List.of(route), List.of(unassigned)
                ));

        DispatchPlanResponse response = service.generatePlan();

        assertNotNull(response.generatedAt());
        assertEquals(2, response.totalOrders());
        assertEquals(1, response.assignedOrders());
        assertEquals(1, response.unassignedOrders());
        assertEquals(new BigDecimal("10.556"), response.totalFleetDistanceKm());

        assertEquals(1, response.vehicles().size());
        var vehicleResponse = response.vehicles().get(0);
        assertEquals("VEH-1", vehicleResponse.vehicleId());
        assertEquals(new BigDecimal("5.000"), vehicleResponse.totalLoad());
        assertEquals(new BigDecimal("15.000"), vehicleResponse.remainingCapacity());
        assertEquals(new BigDecimal("10.556"), vehicleResponse.totalDistanceKm());
        assertEquals("ORD-1", vehicleResponse.assignedOrders().get(0).orderId());
        assertEquals(1, vehicleResponse.assignedOrders().get(0).sequence());

        assertEquals("ORD-2", response.unassigned().get(0).orderId());
        assertEquals(
                UnassignedReason.EXCEEDS_MAX_VEHICLE_CAPACITY,
                response.unassigned().get(0).reason()
        );
        verify(optimizer).optimize(List.of(assignedOrder, unassignedOrder), List.of(vehicle));
    }

    private DeliveryOrder order(String id, String weight) {
        return new DeliveryOrder(
                null, id, 28.62, 77.21, "Address " + id,
                new BigDecimal(weight), Priority.MEDIUM
        );
    }
}
