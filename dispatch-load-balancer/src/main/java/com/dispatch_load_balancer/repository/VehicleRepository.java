package com.dispatch_load_balancer.repository;

import com.dispatch_load_balancer.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VehicleRepository
        extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findAllByBatch_IdOrderByVehicleIdAsc(Long batchId);

    @Modifying
    @Query("DELETE FROM Vehicle v WHERE v.batch.id = :batchId")
    int deleteAllForBatch(@Param("batchId") Long batchId);
}

