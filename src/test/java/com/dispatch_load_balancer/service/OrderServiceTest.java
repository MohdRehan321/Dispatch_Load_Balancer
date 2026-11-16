package com.dispatch_load_balancer.service;

import com.dispatch_load_balancer.entity.DeliveryOrder;
import com.dispatch_load_balancer.entity.DispatchBatch;
import com.dispatch_load_balancer.enums.Priority;
import com.dispatch_load_balancer.repository.DeliveryOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private DeliveryOrderRepository repository;
    private ActiveBatchService activeBatchService;
    private OrderService service;
    private DispatchBatch batch;

    @BeforeEach
    void setUp() {
        repository = mock(DeliveryOrderRepository.class);
        activeBatchService = mock(ActiveBatchService.class);
        service = new OrderService(repository, activeBatchService);
        batch = mock(DispatchBatch.class);
        when(batch.getId()).thenReturn(42L);
        when(activeBatchService.getOrCreateActiveBatch()).thenReturn(batch);
    }

    @Test
    void replaceOrdersDeletesExistingBatchOrdersAssignsBatchAndSavesNewOrders() {
        DeliveryOrder order = new DeliveryOrder(
                null, "ORD-1", 28.61, 77.20, "Delhi",
                new BigDecimal("4.500"), Priority.HIGH
        );

        service.replaceOrders(List.of(order));

        verify(repository).deleteAllForBatch(42L);
        verify(repository).saveAll(List.of(order));
        assertSame(batch, order.getBatch());
    }

    @Test
    void getOrdersReturnsOrdersForActiveBatch() {
        DeliveryOrder order = new DeliveryOrder(
                batch, "ORD-1", 28.61, 77.20, "Delhi",
                new BigDecimal("4.500"), Priority.HIGH
        );
        when(repository.findAllByBatch_IdOrderByOrderIdAsc(42L))
                .thenReturn(List.of(order));

        List<DeliveryOrder> result = service.getOrders();

        assertEquals(1, result.size());
        assertSame(order, result.get(0));
        verify(repository).findAllByBatch_IdOrderByOrderIdAsc(42L);
    }
}
