package com.dispatch_load_balancer.dto.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {}