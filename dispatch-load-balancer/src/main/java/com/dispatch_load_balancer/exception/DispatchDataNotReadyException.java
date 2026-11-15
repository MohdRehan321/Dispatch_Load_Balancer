package com.dispatch_load_balancer.exception;

public class DispatchDataNotReadyException extends RuntimeException {

    public DispatchDataNotReadyException(String message) {
        super(message);
    }
}