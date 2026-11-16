package com.dispatch_load_balancer.optimizer;

import java.util.List;

public record OptimizationResult(
        List<VehicleRoute> routes,
        List<UnassignedOrder> unassignedOrders
) {}

