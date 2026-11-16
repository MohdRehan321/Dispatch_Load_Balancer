package com.dispatch_load_balancer.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.dispatch_load_balancer.entity.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryOrderRepository
        extends JpaRepository<DeliveryOrder, Long> {

    List<DeliveryOrder> findAllByBatch_IdOrderByOrderIdAsc(Long batchId);

    @Modifying
    @Query("DELETE FROM DeliveryOrder o WHERE o.batch.id = :batchId")
    int deleteAllForBatch(@Param("batchId") Long batchId);
}

