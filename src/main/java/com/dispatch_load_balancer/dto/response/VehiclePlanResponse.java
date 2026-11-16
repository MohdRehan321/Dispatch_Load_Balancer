package com.dispatch_load_balancer.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record VehiclePlanResponse(
        String vehicleId,
        String startingAddress,
        BigDecimal capacity,
        BigDecimal totalLoad,
        BigDecimal remainingCapacity,
        List<AssignedOrderResponse> assignedOrders,
        BigDecimal totalDistanceKm
) {}
