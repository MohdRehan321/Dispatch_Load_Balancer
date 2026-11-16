package com.dispatch_load_balancer.service;

import com.dispatch_load_balancer.dto.response.*;
import com.dispatch_load_balancer.exception.DispatchDataNotReadyException;
import com.dispatch_load_balancer.optimizer.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
public class DispatchPlanService {

    private final OrderService orderService;
    private final VehicleService vehicleService;
    private final DispatchOptimizer optimizer;

    public DispatchPlanService(
            OrderService orderService,
            VehicleService vehicleService,
            DispatchOptimizer optimizer) {

        this.orderService = orderService;
        this.vehicleService = vehicleService;
        this.optimizer = optimizer;
    }

    @Transactional
    public DispatchPlanResponse generatePlan() {

        var orders = orderService.getOrders();
        var vehicles = vehicleService.getVehicles();

        if (orders.isEmpty() || vehicles.isEmpty()) {
            throw new DispatchDataNotReadyException(
                    "Upload at least one order and one vehicle before generating a plan"
            );
        }

        OptimizationResult result =
                optimizer.optimize(orders, vehicles);

        List<VehiclePlanResponse> vehicleResponses =
                result.routes().stream().map(route -> {

                    var vehicle = route.vehicle();

                    List<AssignedOrderResponse> assigned =
                            java.util.stream.IntStream
                                    .range(0, route.orders().size())
                                    .mapToObj(index -> {
                                        var order = route.orders().get(index);

                                        return new AssignedOrderResponse(
                                                index + 1,
                                                order.getOrderId(),
                                                order.getAddress(),
                                                order.getLatitude(),
                                                order.getLongitude(),
                                                order.getPackageWeight(),
                                                order.getPriority()
                                        );
                                    })
                                    .toList();

                    BigDecimal remaining =
                            vehicle.getCapacity().subtract(route.totalLoad());

                    return new VehiclePlanResponse(
                            vehicle.getVehicleId(),
                            vehicle.getCurrentAddress(),
                            vehicle.getCapacity(),
                            route.totalLoad(),
                            remaining,
                            assigned,
                            roundDistance(route.totalDistanceKm())
                    );
                }).toList();

        List<UnassignedOrderResponse> unassigned =
                result.unassignedOrders().stream()
                        .map(item -> {
                            var order = item.order();

                            return new UnassignedOrderResponse(
                                    order.getOrderId(),
                                    order.getAddress(),
                                    order.getPackageWeight(),
                                    order.getPriority(),
                                    item.reason()
                            );
                        })
                        .toList();

        BigDecimal totalDistance = result.routes().stream()
                .map(route -> roundDistance(route.totalDistanceKm()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int assignedCount = result.routes().stream()
                .mapToInt(route -> route.orders().size())
                .sum();

        return new DispatchPlanResponse(
                Instant.now(),
                orders.size(),
                assignedCount,
                unassigned.size(),
                totalDistance,
                vehicleResponses,
                unassigned
        );
    }

    private BigDecimal roundDistance(double distance) {
        return BigDecimal.valueOf(distance)
                .setScale(3, RoundingMode.HALF_UP);
    }
}

