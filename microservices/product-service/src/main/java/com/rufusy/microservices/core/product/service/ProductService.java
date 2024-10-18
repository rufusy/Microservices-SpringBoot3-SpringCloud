package com.rufusy.microservices.core.product.service;

import com.rufusy.microservices.api.core.product.Product;
import reactor.core.publisher.Mono;

public interface ProductService {
    Mono<Product> getProduct(int productId, int delay, int faultPercent);

    Mono<Product> createProduct(Product body);

    Mono<Void> deleteProductById(int productId);
}
