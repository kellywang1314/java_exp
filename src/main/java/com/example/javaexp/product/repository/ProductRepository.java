package com.example.javaexp.product.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.javaexp.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryOrderByUpdatedAtDesc(String category);

    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);

    @Query(
            value = """
                    SELECT *
                    FROM products
                    WHERE price >= :minPrice
                    ORDER BY price DESC
                    """,
            nativeQuery = true)
    List<Product> findByMinimumPrice(@Param("minPrice") BigDecimal minPrice);
}
