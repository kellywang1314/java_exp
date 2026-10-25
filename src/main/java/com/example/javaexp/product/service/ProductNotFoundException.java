package com.example.javaexp.product.service;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Product " + productId + " was not found");
    }
}
