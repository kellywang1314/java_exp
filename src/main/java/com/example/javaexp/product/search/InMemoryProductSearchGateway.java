package com.example.javaexp.product.search;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.example.javaexp.product.api.ProductResponse;

@Component
@Profile("!infra")
public class InMemoryProductSearchGateway implements ProductSearchGateway {

    private final Map<Long, ProductResponse> index = new ConcurrentHashMap<>();

    @Override
    public void save(ProductResponse product) {
        index.put(product.id(), product);
    }

    @Override
    public void delete(Long productId) {
        index.remove(productId);
    }

    @Override
    public List<ProductResponse> search(String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        return index.values().stream()
                .filter(product -> contains(product.name(), normalizedKeyword)
                        || contains(product.category(), normalizedKeyword)
                        || contains(product.description(), normalizedKeyword))
                .sorted(Comparator.comparing(ProductResponse::updatedAt).reversed())
                .toList();
    }

    @Override
    public Page<ProductResponse> advancedSearch(
            ProductSearchCriteria criteria,
            Pageable pageable) {
        String normalizedKeyword = criteria.keyword().toLowerCase(Locale.ROOT);
        List<ProductResponse> products = index.values().stream()
                .filter(product -> contains(product.name(), normalizedKeyword)
                        || contains(product.description(), normalizedKeyword))
                .filter(product -> criteria.category() == null
                        || criteria.category().isBlank()
                        || product.category().equalsIgnoreCase(criteria.category()))
                .filter(product -> criteria.minPrice() == null
                        || product.price().compareTo(criteria.minPrice()) >= 0)
                .filter(product -> criteria.maxPrice() == null
                        || product.price().compareTo(criteria.maxPrice()) <= 0)
                .sorted(searchComparator(pageable.getSort()))
                .toList();

        int fromIndex = (int) Math.min(pageable.getOffset(), products.size());
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), products.size());
        return new PageImpl<>(
                products.subList(fromIndex, toIndex),
                pageable,
                products.size());
    }

    private boolean contains(String value, String keyword) {
        return value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private Comparator<ProductResponse> searchComparator(Sort sort) {
        return sort.stream()
                .map(this::comparatorFor)
                .reduce(Comparator::thenComparing)
                .orElse(Comparator.comparing(ProductResponse::updatedAt).reversed());
    }

    private Comparator<ProductResponse> comparatorFor(Sort.Order order) {
        Comparator<ProductResponse> comparator = switch (order.getProperty()) {
            case "name" -> Comparator.comparing(
                    ProductResponse::name,
                    String.CASE_INSENSITIVE_ORDER);
            case "price" -> Comparator.comparing(ProductResponse::price);
            case "createdAt" -> Comparator.comparing(ProductResponse::createdAt);
            default -> Comparator.comparing(ProductResponse::updatedAt);
        };
        return order.isDescending() ? comparator.reversed() : comparator;
    }
}
