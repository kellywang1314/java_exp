package com.example.javaexp.product.search;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
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

    @Override
    public Page<ProductResponse> advancedSearch(
            ProductSearchCriteria criteria,
            Pageable pageable) {
        Criteria queryCriteria = new Criteria("name").matches(criteria.keyword())
                .or(new Criteria("description").matches(criteria.keyword()));

        if (criteria.category() != null && !criteria.category().isBlank()) {
            queryCriteria = queryCriteria.and(
                    new Criteria("category").is(criteria.category()));
        }
        if (criteria.minPrice() != null) {
            queryCriteria = queryCriteria.and(
                    new Criteria("price").greaterThanEqual(criteria.minPrice()));
        }
        if (criteria.maxPrice() != null) {
            queryCriteria = queryCriteria.and(
                    new Criteria("price").lessThanEqual(criteria.maxPrice()));
        }

        CriteriaQuery query = new CriteriaQuery(queryCriteria);
        query.setPageable(pageable);
        SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);
        List<ProductResponse> products = hits.getSearchHits().stream()
                .map(hit -> hit.getContent().toResponse())
                .toList();
        return new PageImpl<>(products, pageable, hits.getTotalHits());
    }
}
