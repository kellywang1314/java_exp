package com.example.javaexp.product.search;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
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

    private boolean contains(String value, String keyword) {
        return value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
