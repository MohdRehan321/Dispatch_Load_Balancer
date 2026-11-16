package com.dispatch_load_balancer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private DispatchBatch batch;

    @Column(name = "vehicle_id", nullable = false, length = 100)
    private String vehicleId;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal capacity;

    @Column(name = "current_latitude", nullable = false)
    private Double currentLatitude;

    @Column(name = "current_longitude", nullable = false)
    private Double currentLongitude;

    @Column(name = "current_address", nullable = false, length = 500)
    private String currentAddress;

    protected Vehicle() {
    }

    public Vehicle(
            DispatchBatch batch,
            String vehicleId,
            BigDecimal capacity,
            Double currentLatitude,
            Double currentLongitude,
            String currentAddress) {

        this.batch = batch;
        this.vehicleId = vehicleId;
        this.capacity = capacity;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.currentAddress = currentAddress;
    }

    public Long getId() {
        return id;
    }

    public DispatchBatch getBatch() {
        return batch;
    }

    public void setBatch(DispatchBatch batch) {
        this.batch = batch;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public BigDecimal getCapacity() {
        return capacity;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }
}
