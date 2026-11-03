package com.example.campusshop.product.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "ProductInput", description = "Fields accepted when creating or updating a product.")
public record ProductRequest(
        @NotBlank(message = "Name is required.")
        @Size(max = 120, message = "Name must be 120 characters or fewer.")
        String name,

        @Size(max = 2000, message = "Description must be 2000 characters or fewer.")
        String description,

        @NotNull(message = "Price is required.")
        @DecimalMin(value = "0.00", message = "Price cannot be negative.")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits and 2 decimals.")
        BigDecimal price,

        @NotNull(message = "Stock is required.")
        @Min(value = 0, message = "Stock cannot be negative.")
        Integer stock,

        @NotBlank(message = "Category is required.")
        @Size(max = 40, message = "Category must be 40 characters or fewer.")
        String category) {
}
