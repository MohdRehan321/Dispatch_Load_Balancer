package com.dispatch_load_balancer.dto.response;

import com.dispatch_load_balancer.enums.Priority;
import com.dispatch_load_balancer.optimizer.UnassignedReason;

import java.math.BigDecimal;

public record UnassignedOrderResponse(
        String orderId,
        String address,
        BigDecimal packageWeight,
        Priority priority,
        UnassignedReason reason
) {}

