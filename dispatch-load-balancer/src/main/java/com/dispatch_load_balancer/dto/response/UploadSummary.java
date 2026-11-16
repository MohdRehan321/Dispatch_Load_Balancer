package com.dispatch_load_balancer.dto.response;

import java.util.List;

public record UploadSummary(
        int received,
        int saved,
        List<String> ids
) {}