package com.example.javaexp.product.search;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;

import com.example.javaexp.product.api.ProductResponse;

@Component
@Profile("infra")
public class ElasticsearchProductSearchGateway implements ProductSearchGateway {

    private final ElasticsearchOperations operations;

    public ElasticsearchProductSearchGateway(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    @PostConstruct
    void createIndex() {
        IndexOperations indexOperations = operations.indexOps(ProductDocument.class);
        if (!indexOperations.exists()) {
            indexOperations.createWithMapping();
        }
    }

    @Override
    public void save(ProductResponse product) {
        operations.save(ProductDocument.from(product));
    }

    @Override
    public void delete(Long productId) {
        operations.delete(productId.toString(), ProductDocument.class);
    }

    @Override
    public List<ProductResponse> search(String keyword) {
        Criteria criteria = new Criteria("name").contains(keyword)
                .or(new Criteria("category").contains(keyword))
                .or(new Criteria("description").contains(keyword));

        return operations.search(new CriteriaQuery(criteria), ProductDocument.class)
                .getSearchHits()
                .stream()
                .map(hit -> hit.getContent().toResponse())
                .toList();
    }
}
