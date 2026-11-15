package com.dispatch_load_balancer.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DispatchPlanResponse(
        Instant generatedAt,
        int totalOrders,
        int assignedOrders,
        int unassignedOrders,
        BigDecimal totalFleetDistanceKm,
        List<VehiclePlanResponse> vehicles,
        List<UnassignedOrderResponse> unassigned
) {}
