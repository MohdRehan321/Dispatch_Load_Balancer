package com.dispatch_load_balancer.service;

import com.dispatch_load_balancer.entity.DeliveryOrder;
import com.dispatch_load_balancer.entity.DispatchBatch;
import com.dispatch_load_balancer.repository.DeliveryOrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final DeliveryOrderRepository orderRepository;
    private final ActiveBatchService activeBatchService;

    public OrderService(
            DeliveryOrderRepository orderRepository,
            ActiveBatchService activeBatchService) {
        this.orderRepository = orderRepository;
        this.activeBatchService = activeBatchService;
    }

    @Transactional
    public void replaceOrders(List<DeliveryOrder> orders) {

        DispatchBatch batch =
                activeBatchService.getOrCreateActiveBatch();

        orderRepository.deleteAllForBatch(batch.getId());

        orders.forEach(order -> order.setBatch(batch));

        orderRepository.saveAll(orders);
    }

    @Transactional
    public List<DeliveryOrder> getOrders() {
        DispatchBatch batch = activeBatchService.getOrCreateActiveBatch();

        return orderRepository
                .findAllByBatch_IdOrderByOrderIdAsc(batch.getId());
    }
}

