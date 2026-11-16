package com.dispatch_load_balancer.controller;

import com.dispatch_load_balancer.dto.request.OrderInputDto;
import com.dispatch_load_balancer.dto.request.OrderRequest;
import com.dispatch_load_balancer.dto.request.VehicleInputDto;
import com.dispatch_load_balancer.dto.request.VehicleRequest;
import com.dispatch_load_balancer.dto.response.ApiResponse;
import com.dispatch_load_balancer.dto.response.DispatchPlanResponse;
import com.dispatch_load_balancer.dto.response.UploadSummary;
import com.dispatch_load_balancer.entity.DeliveryOrder;
import com.dispatch_load_balancer.entity.Vehicle;
import com.dispatch_load_balancer.enums.Priority;
import com.dispatch_load_balancer.exception.DuplicateRequestException;
import com.dispatch_load_balancer.mapper.DispatchMapper;
import com.dispatch_load_balancer.service.DispatchPlanService;
import com.dispatch_load_balancer.service.OrderService;
import com.dispatch_load_balancer.service.VehicleService;
import com.dispatch_load_balancer.validation.RequestIdValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DispatchControllerTest {

    private OrderService orderService;
    private VehicleService vehicleService;
    private DispatchMapper mapper;
    private DispatchPlanService planService;
    private DispatchController controller;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        vehicleService = mock(VehicleService.class);
        mapper = mock(DispatchMapper.class);
        planService = mock(DispatchPlanService.class);
        controller = new DispatchController(
                orderService, vehicleService, mapper,
                new RequestIdValidator(), planService
        );
    }

    @Test
    void uploadOrdersReturnsSummaryAndPersistsMappedOrders() {
        OrderInputDto dto = new OrderInputDto(
                "ORD-1", 28.61, 77.20, "Delhi",
                new BigDecimal("5.000"), Priority.HIGH
        );
        DeliveryOrder entity = new DeliveryOrder(
                null, "ORD-1", 28.61, 77.20, "Delhi",
                new BigDecimal("5.000"), Priority.HIGH
        );
        when(mapper.toOrderEntity(dto)).thenReturn(entity);

        ResponseEntity<ApiResponse<UploadSummary>> response =
                controller.uploadOrders(new OrderRequest(List.of(dto)));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertEquals("Orders saved successfully", response.getBody().message());
        assertEquals(1, response.getBody().data().received());
        assertEquals(1, response.getBody().data().saved());
        assertEquals(List.of("ORD-1"), response.getBody().data().ids());
        verify(orderService).replaceOrders(List.of(entity));
    }

    @Test
    void uploadVehiclesReturnsSummaryAndPersistsMappedVehicles() {
        VehicleInputDto dto = new VehicleInputDto(
                "VEH-1", new BigDecimal("100.000"),
                28.61, 77.20, "Delhi Depot"
        );
        Vehicle entity = new Vehicle(
                null, "VEH-1", new BigDecimal("100.000"),
                28.61, 77.20, "Delhi Depot"
        );
        when(mapper.toVehicleEntity(dto)).thenReturn(entity);

        ResponseEntity<ApiResponse<UploadSummary>> response =
                controller.uploadVehicles(new VehicleRequest(List.of(dto)));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertEquals("Vehicles saved successfully", response.getBody().message());
        assertEquals(1, response.getBody().data().received());
        assertEquals(1, response.getBody().data().saved());
        assertEquals(List.of("VEH-1"), response.getBody().data().ids());
        verify(vehicleService).replaceVehicles(List.of(entity));
    }

    @Test
    void uploadOrdersRejectsDuplicateIdsBeforeMappingOrPersistence() {
        OrderInputDto first = new OrderInputDto(
                "ORD-DUP", 28.61, 77.20, "Address 1",
                new BigDecimal("5.000"), Priority.HIGH
        );
        OrderInputDto second = new OrderInputDto(
                " ORD-DUP ", 28.62, 77.21, "Address 2",
                new BigDecimal("4.000"), Priority.LOW
        );

        DuplicateRequestException exception = assertThrows(
                DuplicateRequestException.class,
                () -> controller.uploadOrders(new OrderRequest(List.of(first, second)))
        );

        assertEquals("Duplicate order ID: ORD-DUP", exception.getMessage());
        verifyNoInteractions(mapper, orderService);
    }

    @Test
    void uploadVehiclesRejectsDuplicateIdsBeforeMappingOrPersistence() {
        VehicleInputDto first = new VehicleInputDto(
                "VEH-DUP", new BigDecimal("100.000"),
                28.61, 77.20, "Depot 1"
        );
        VehicleInputDto second = new VehicleInputDto(
                " VEH-DUP ", new BigDecimal("120.000"),
                28.62, 77.21, "Depot 2"
        );

        DuplicateRequestException exception = assertThrows(
                DuplicateRequestException.class,
                () -> controller.uploadVehicles(new VehicleRequest(List.of(first, second)))
        );

        assertEquals("Duplicate vehicle ID: VEH-DUP", exception.getMessage());
        verifyNoInteractions(mapper, vehicleService);
    }

    @Test
    void getPlanWrapsGeneratedPlanInSuccessfulApiResponse() {
        DispatchPlanResponse plan = new DispatchPlanResponse(
                java.time.Instant.parse("2026-01-01T00:00:00Z"),
                0, 0, 0, BigDecimal.ZERO, List.of(), List.of()
        );
        when(planService.generatePlan()).thenReturn(plan);

        ResponseEntity<ApiResponse<DispatchPlanResponse>> response =
                controller.getPlan();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertSame(plan, response.getBody().data());
        verify(planService).generatePlan();
    }
}
