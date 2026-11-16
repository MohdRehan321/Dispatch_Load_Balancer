package com.dispatch_load_balancer.dto.request;

import com.dispatch_load_balancer.enums.Priority;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record OrderInputDto(

        @NotBlank
        @Size(max = 100)
        String orderId,

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double latitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double longitude,

        @NotBlank
        @Size(max = 500)
        String address,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 9, fraction = 3)
        BigDecimal packageWeight,

        @NotNull
        Priority priority

) {}

