package com.dispatch_load_balancer.dto.response;
import com.dispatch_load_balancer.enums.Priority;

import java.math.BigDecimal;

public record AssignedOrderResponse(
        int sequence,
        String orderId,
        String address,
        Double latitude,
        Double longitude,
        BigDecimal packageWeight,
        Priority priority
) {}

