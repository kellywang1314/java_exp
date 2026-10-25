package com.example.javaexp.product.search;

import java.math.BigDecimal;

public record ProductSearchCriteria(
        String keyword,
        String category,
        BigDecimal minPrice,
        BigDecimal maxPrice) {
}
