package com.example.javaexp.product.search;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.example.javaexp.product.api.ProductResponse;

@Document(indexName = "products")
public record ProductDocument(
        @Id Long id,
        @Field(type = FieldType.Text) String name,
        @Field(type = FieldType.Keyword) String category,
        @Field(type = FieldType.Scaled_Float, scalingFactor = 100) BigDecimal price,
        @Field(type = FieldType.Text) String description,
        @Field(type = FieldType.Date) LocalDateTime createdAt,
        @Field(type = FieldType.Date) LocalDateTime updatedAt) {

    static ProductDocument from(ProductResponse product) {
        return new ProductDocument(
                product.id(),
                product.name(),
                product.category(),
                product.price(),
                product.description(),
                product.createdAt(),
                product.updatedAt());
    }

    ProductResponse toResponse() {
        return new ProductResponse(
                id,
                name,
                category,
                price,
                description,
                createdAt,
                updatedAt);
    }
}
