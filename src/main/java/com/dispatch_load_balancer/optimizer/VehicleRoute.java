package com.dispatch_load_balancer.optimizer;

import com.dispatch_load_balancer.entity.DeliveryOrder;
import com.dispatch_load_balancer.entity.Vehicle;

import java.math.BigDecimal;
import java.util.List;

public record VehicleRoute(
        Vehicle vehicle,
        List<DeliveryOrder> orders,
        BigDecimal totalLoad,
        double totalDistanceKm
) {}

