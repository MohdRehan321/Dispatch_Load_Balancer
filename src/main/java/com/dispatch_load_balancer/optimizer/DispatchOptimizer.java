package com.dispatch_load_balancer.optimizer;

import com.dispatch_load_balancer.entity.DeliveryOrder;
import com.dispatch_load_balancer.entity.Vehicle;

import java.util.List;

public interface DispatchOptimizer {

    OptimizationResult optimize(
            List<DeliveryOrder> orders,
            List<Vehicle> vehicles
    );
}
