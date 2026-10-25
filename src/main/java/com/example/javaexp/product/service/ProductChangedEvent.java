package com.example.javaexp.product.service;

import com.example.javaexp.product.api.ProductResponse;

public record ProductChangedEvent(Type type, ProductResponse product) {

    public enum Type {
        UPSERT,
        DELETE
    }
}
