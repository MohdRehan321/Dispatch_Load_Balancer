package com.dispatch_load_balancer.repository;

import com.dispatch_load_balancer.entity.DispatchBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DispatchBatchRepository
        extends JpaRepository<DispatchBatch, Long> {

    Optional<DispatchBatch> findFirstByActiveTrue();

    @Modifying
    @Query(value = """
        INSERT INTO dispatch_batches
            (is_active, created_at, updated_at)
        SELECT TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        WHERE NOT EXISTS (
            SELECT 1 FROM dispatch_batches WHERE is_active = TRUE
        )
        ON CONFLICT (is_active) WHERE is_active = TRUE
        DO NOTHING
        """, nativeQuery = true)
    void createActiveBatchIfMissing();
}
