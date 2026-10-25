package com.example.javaexp.product.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.javaexp.product.model.Product;

public record ProductResponse(
        Long id,
        String name,
        String category,
        BigDecimal price,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getDescription(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
