package com.dispatch_load_balancer.service;

import com.dispatch_load_balancer.entity.DispatchBatch;
import com.dispatch_load_balancer.entity.Vehicle;
import com.dispatch_load_balancer.repository.VehicleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ActiveBatchService activeBatchService;

    public VehicleService(
            VehicleRepository vehicleRepository,
            ActiveBatchService activeBatchService) {
        this.vehicleRepository = vehicleRepository;
        this.activeBatchService = activeBatchService;
    }

    @Transactional
    public void replaceVehicles(List<Vehicle> vehicles) {

        DispatchBatch batch =
                activeBatchService.getOrCreateActiveBatch();

        vehicleRepository.deleteAllForBatch(batch.getId());

        vehicles.forEach(vehicle -> vehicle.setBatch(batch));

        vehicleRepository.saveAll(vehicles);
    }

    @Transactional
    public List<Vehicle> getVehicles() {

        DispatchBatch batch =
                activeBatchService.getOrCreateActiveBatch();

        return vehicleRepository
                .findAllByBatch_IdOrderByVehicleIdAsc(batch.getId());
    }
}

