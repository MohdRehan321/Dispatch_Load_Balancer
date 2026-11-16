package com.dispatch_load_balancer.validation;

import com.dispatch_load_balancer.dto.request.OrderInputDto;
import com.dispatch_load_balancer.dto.request.OrderRequest;
import com.dispatch_load_balancer.dto.request.VehicleInputDto;
import com.dispatch_load_balancer.dto.request.VehicleRequest;
import com.dispatch_load_balancer.enums.Priority;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RequestDtoValidationTest {

    private static Validator validator;
    private static jakarta.validation.ValidatorFactory factory;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void orderInputRejectsInvalidFields() {
        OrderInputDto invalid = new OrderInputDto(
                " ", 91.0, 181.0, " ",
                BigDecimal.ZERO, null
        );

        Set<String> invalidFields = validator.validate(invalid).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(invalidFields.containsAll(Set.of(
                "orderId", "latitude", "longitude",
                "address", "packageWeight", "priority"
        )));
    }

    @Test
    void orderRequestRejectsEmptyListAndValidatesNestedOrders() {
        assertFalse(validator.validate(new OrderRequest(List.of())).isEmpty());

        OrderInputDto invalidOrder = new OrderInputDto(
                "", 0.0, 0.0, "Valid address",
                new BigDecimal("1.000"), Priority.HIGH
        );
        assertFalse(validator.validate(
                new OrderRequest(List.of(invalidOrder))
        ).isEmpty());
    }

    @Test
    void vehicleInputRejectsInvalidCapacityAndCoordinates() {
        VehicleInputDto invalid = new VehicleInputDto(
                "", BigDecimal.ZERO, -91.0, 181.0, ""
        );

        Set<String> invalidFields = validator.validate(invalid).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(invalidFields.containsAll(Set.of(
                "vehicleId", "capacity", "currentLatitude",
                "currentLongitude", "currentAddress"
        )));
    }

    @Test
    void vehicleRequestRejectsEmptyListAndValidatesNestedVehicles() {
        assertFalse(
                validator.validate(new VehicleRequest(List.of())).isEmpty()
        );

        VehicleInputDto invalidVehicle = new VehicleInputDto(
                " ", new BigDecimal("10.000"),
                0.0, 0.0, "Depot"
        );

        assertFalse(
                validator.validate(
                        new VehicleRequest(List.of(invalidVehicle))
                ).isEmpty()
        );
    }
}
