package com.dispatch_load_balancer.mapper;

import com.dispatch_load_balancer.dto.request.OrderInputDto;
import com.dispatch_load_balancer.dto.request.VehicleInputDto;
import com.dispatch_load_balancer.entity.DeliveryOrder;
import com.dispatch_load_balancer.entity.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class DispatchMapper {

    public DeliveryOrder toOrderEntity(OrderInputDto dto) {
        return new DeliveryOrder(
                null,
                dto.orderId().trim(),
                dto.latitude(),
                dto.longitude(),
                dto.address().trim(),
                dto.packageWeight(),
                dto.priority()
        );
    }

    public Vehicle toVehicleEntity(VehicleInputDto dto) {
        return new Vehicle(
                null,
                dto.vehicleId().trim(),
                dto.capacity(),
                dto.currentLatitude(),
                dto.currentLongitude(),
                dto.currentAddress().trim()
        );
    }
}