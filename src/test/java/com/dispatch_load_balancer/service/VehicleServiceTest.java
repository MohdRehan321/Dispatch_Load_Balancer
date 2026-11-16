package com.dispatch_load_balancer.service;

import com.dispatch_load_balancer.entity.DispatchBatch;
import com.dispatch_load_balancer.entity.Vehicle;
import com.dispatch_load_balancer.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleServiceTest {

    private VehicleRepository repository;
    private ActiveBatchService activeBatchService;
    private VehicleService service;
    private DispatchBatch batch;

    @BeforeEach
    void setUp() {
        repository = mock(VehicleRepository.class);
        activeBatchService = mock(ActiveBatchService.class);
        service = new VehicleService(repository, activeBatchService);
        batch = mock(DispatchBatch.class);
        when(batch.getId()).thenReturn(9L);
        when(activeBatchService.getOrCreateActiveBatch()).thenReturn(batch);
    }

    @Test
    void replaceVehiclesDeletesExistingBatchVehiclesAssignsBatchAndSavesNewVehicles() {
        Vehicle vehicle = new Vehicle(
                null, "VEH-1", new BigDecimal("100.000"),
                28.61, 77.20, "Delhi Depot"
        );

        service.replaceVehicles(List.of(vehicle));

        verify(repository).deleteAllForBatch(9L);
        verify(repository).saveAll(List.of(vehicle));
        assertSame(batch, vehicle.getBatch());
    }

    @Test
    void getVehiclesReturnsVehiclesForActiveBatch() {
        Vehicle vehicle = new Vehicle(
                batch, "VEH-1", new BigDecimal("100.000"),
                28.61, 77.20, "Delhi Depot"
        );
        when(repository.findAllByBatch_IdOrderByVehicleIdAsc(9L))
                .thenReturn(List.of(vehicle));

        List<Vehicle> result = service.getVehicles();

        assertEquals(1, result.size());
        assertSame(vehicle, result.get(0));
        verify(repository).findAllByBatch_IdOrderByVehicleIdAsc(9L);
    }
}
