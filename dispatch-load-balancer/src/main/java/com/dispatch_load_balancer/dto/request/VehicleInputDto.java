package com.dispatch_load_balancer.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record VehicleInputDto(

        @NotBlank
        @Size(max = 100)
        String vehicleId,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 9, fraction = 3)
        BigDecimal capacity,

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double currentLatitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double currentLongitude,

        @NotBlank
        @Size(max = 500)
        String currentAddress

) {}

