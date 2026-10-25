package com.example.javaexp.product.search;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.javaexp.product.api.ProductResponse;

public interface ProductSearchGateway {

    void save(ProductResponse product);

    void delete(Long productId);

    List<ProductResponse> search(String keyword);

    Page<ProductResponse> advancedSearch(
            ProductSearchCriteria criteria,
            Pageable pageable);
}
