package com.dispatch_load_balancer.entity;

import com.dispatch_load_balancer.enums.Priority;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "delivery_orders")
public class DeliveryOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private DispatchBatch batch;

    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(name = "package_weight", nullable = false,
            precision = 12, scale = 3)
    private BigDecimal packageWeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    protected DeliveryOrder() {
    }

    public DeliveryOrder(
            DispatchBatch batch,
            String orderId,
            Double latitude,
            Double longitude,
            String address,
            BigDecimal packageWeight,
            Priority priority) {

        this.batch = batch;
        this.orderId = orderId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.packageWeight = packageWeight;
        this.priority = priority;
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

    public String getOrderId() {
        return orderId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getPackageWeight() {
        return packageWeight;
    }

    public Priority getPriority() {
        return priority;
    }
}
