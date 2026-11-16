package com.dispatch_load_balancer.optimizer;

import com.dispatch_load_balancer.entity.DeliveryOrder;

public record UnassignedOrder(
        DeliveryOrder order,
        UnassignedReason reason
) {}
