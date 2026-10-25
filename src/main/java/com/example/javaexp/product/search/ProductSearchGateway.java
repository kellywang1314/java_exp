package com.example.javaexp.product.search;

import java.util.List;

import com.example.javaexp.product.api.ProductResponse;

public interface ProductSearchGateway {

    void save(ProductResponse product);

    void delete(Long productId);

    List<ProductResponse> search(String keyword);
}
