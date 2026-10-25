package com.example.javaexp.product.api;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.javaexp.product.service.ProductService;

@Validated
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.create(request);
        return ResponseEntity
                .created(URI.create("/api/products/" + product.id()))
                .body(product);
    }

    @GetMapping("/{productId}")
    public ProductResponse get(@PathVariable Long productId) {
        return productService.get(productId);
    }

    @GetMapping
    public List<ProductResponse> list(@RequestParam(required = false) String category) {
        return productService.list(category);
    }

    @GetMapping("/page")
    public PageResponse<ProductResponse> page(
            @RequestParam(required = false) String category,
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return PageResponse.from(productService.page(category, pageable));
    }

    @GetMapping("/search")
    public List<ProductResponse> search(
            @RequestParam
            @NotBlank
            @Size(max = 100)
            String keyword) {
        return productService.search(keyword);
    }

    @GetMapping("/price-above")
    public List<ProductResponse> listByMinimumPrice(
            @RequestParam
            @DecimalMin(value = "0.00")
            BigDecimal minPrice) {
        return productService.listByMinimumPrice(minPrice);
    }

    @PutMapping("/{productId}")
    public ProductResponse update(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request) {
        return productService.update(productId, request);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId) {
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }
}
