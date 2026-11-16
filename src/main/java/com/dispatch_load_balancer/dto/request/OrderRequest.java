package com.dispatch_load_balancer.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequest(

        @NotEmpty
        List<@NotNull @Valid OrderInputDto> orders

) {}
