package com.example.campusshop.product.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Product", description = "A product in the Campus Shop catalog.")
public record ProductResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String description,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal price,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer stock,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String category,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime createdAt,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime updatedAt) {
}
