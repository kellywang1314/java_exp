package com.example.javaexp.product.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.javaexp.product.api.ProductRequest;
import com.example.javaexp.product.api.ProductResponse;
import com.example.javaexp.product.model.Product;
import com.example.javaexp.product.repository.ProductRepository;
import com.example.javaexp.product.search.ProductSearchCriteria;
import com.example.javaexp.product.search.ProductSearchGateway;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductSearchGateway productSearchGateway;
    private final ApplicationEventPublisher eventPublisher;

    public ProductService(
            ProductRepository productRepository,
            ProductSearchGateway productSearchGateway,
            ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.productSearchGateway = productSearchGateway;
        this.eventPublisher = eventPublisher;
    }

    @CachePut(cacheNames = "products", key = "#result.id()")
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product(
                request.name(),
                request.category(),
                request.price(),
                request.description());
        ProductResponse response = ProductResponse.from(productRepository.saveAndFlush(product));
        publishChange(ProductChangedEvent.Type.UPSERT, response);
        return response;
    }

    @Cacheable(cacheNames = "products", key = "#productId")
    @Transactional(readOnly = true)
    public ProductResponse get(Long productId) {
        return ProductResponse.from(findProduct(productId));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(String category) {
        List<Product> products = category == null || category.isBlank()
                ? productRepository.findAll()
                : productRepository.findByCategoryOrderByUpdatedAtDesc(category);
        return products.stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> page(String category, Pageable pageable) {
        Page<Product> products = category == null || category.isBlank()
                ? productRepository.findAll(pageable)
                : productRepository.findByCategoryIgnoreCase(category, pageable);
        return products.map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listByMinimumPrice(BigDecimal minPrice) {
        return productRepository.findByMinimumPrice(minPrice).stream()
                .map(ProductResponse::from)
                .toList();
    }

    public List<ProductResponse> search(String keyword) {
        return productSearchGateway.search(keyword.trim());
    }

    public Page<ProductResponse> advancedSearch(
            String keyword,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidSearchCriteriaException(
                    "minPrice must be less than or equal to maxPrice");
        }
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                keyword.trim(),
                category == null ? null : category.trim(),
                minPrice,
                maxPrice);
        return productSearchGateway.advancedSearch(criteria, pageable);
    }

    @CachePut(cacheNames = "products", key = "#productId")
    @Transactional
    public ProductResponse update(Long productId, ProductRequest request) {
        Product product = findProduct(productId);
        product.update(
                request.name(),
                request.category(),
                request.price(),
                request.description());
        ProductResponse response = ProductResponse.from(productRepository.saveAndFlush(product));
        publishChange(ProductChangedEvent.Type.UPSERT, response);
        return response;
    }

    @CacheEvict(cacheNames = "products", key = "#productId")
    @Transactional
    public void delete(Long productId) {
        ProductResponse response = ProductResponse.from(findProduct(productId));
        productRepository.deleteById(productId);
        publishChange(ProductChangedEvent.Type.DELETE, response);
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void publishChange(ProductChangedEvent.Type type, ProductResponse product) {
        eventPublisher.publishEvent(new ProductChangedEvent(type, product));
    }
}
