package com.dispatch_load_balancer.controller;

import com.dispatch_load_balancer.dto.request.OrderRequest;
import com.dispatch_load_balancer.dto.request.VehicleRequest;
import com.dispatch_load_balancer.dto.response.ApiResponse;
import com.dispatch_load_balancer.dto.response.UploadSummary;
import com.dispatch_load_balancer.entity.DeliveryOrder;
import com.dispatch_load_balancer.entity.Vehicle;
import com.dispatch_load_balancer.mapper.DispatchMapper;
import com.dispatch_load_balancer.service.OrderService;
import com.dispatch_load_balancer.service.VehicleService;
import com.dispatch_load_balancer.validation.RequestIdValidator;
import com.dispatch_load_balancer.dto.response.DispatchPlanResponse;
import com.dispatch_load_balancer.service.DispatchPlanService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    private final OrderService orderService;
    private final VehicleService vehicleService;
    private final DispatchMapper mapper;
    private final RequestIdValidator idValidator;
    private final DispatchPlanService dispatchPlanService;

    public DispatchController(
            OrderService orderService,
            VehicleService vehicleService,
            DispatchMapper mapper,
            RequestIdValidator idValidator,
            DispatchPlanService dispatchPlanService) {

        this.orderService = orderService;
        this.vehicleService = vehicleService;
        this.mapper = mapper;
        this.idValidator = idValidator;
        this.dispatchPlanService = dispatchPlanService;
    }

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<UploadSummary>> uploadOrders(
            @Valid @RequestBody OrderRequest request) {

        idValidator.validateUniqueIds(
                request.orders(),
                dto -> dto.orderId(),
                "order"
        );

        List<DeliveryOrder> orders = request.orders()
                .stream()
                .map(mapper::toOrderEntity)
                .toList();

        orderService.replaceOrders(orders);

        List<String> ids = orders.stream()
                .map(DeliveryOrder::getOrderId)
                .toList();

        UploadSummary summary = new UploadSummary(
                orders.size(), orders.size(), ids
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(true, "Orders saved successfully", summary)
        );
    }

    @PostMapping("/vehicles")
    public ResponseEntity<ApiResponse<UploadSummary>> uploadVehicles(
            @Valid @RequestBody VehicleRequest request) {

        idValidator.validateUniqueIds(
                request.vehicles(),
                dto -> dto.vehicleId(),
                "vehicle"
        );

        List<Vehicle> vehicles = request.vehicles()
                .stream()
                .map(mapper::toVehicleEntity)
                .toList();

        vehicleService.replaceVehicles(vehicles);

        List<String> ids = vehicles.stream()
                .map(Vehicle::getVehicleId)
                .toList();

        UploadSummary summary = new UploadSummary(
                vehicles.size(), vehicles.size(), ids
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(true, "Vehicles saved successfully", summary)
        );
    }

    @GetMapping("/plan")
    public ResponseEntity<ApiResponse<DispatchPlanResponse>> getPlan() {

        DispatchPlanResponse plan =   dispatchPlanService.generatePlan();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Dispatch plan generated successfully",
                        plan
                )
        );
    }
}
