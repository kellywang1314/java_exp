package com.example.javaexp.product.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.javaexp.product.search.ProductSearchGateway;

@Component
public class ProductIndexListener {

    private final ProductSearchGateway productSearchGateway;

    public ProductIndexListener(ProductSearchGateway productSearchGateway) {
        this.productSearchGateway = productSearchGateway;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateSearchIndex(ProductChangedEvent event) {
        if (event.type() == ProductChangedEvent.Type.DELETE) {
            productSearchGateway.delete(event.product().id());
            return;
        }
        productSearchGateway.save(event.product());
    }
}
