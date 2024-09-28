package com.rufusy.microservices.core.product.service;

import com.rufusy.microservices.api.core.product.Product;
import reactor.core.publisher.Mono;

public interface ProductService {
    Mono<Product> getProduct(int productId);

    Mono<Product> createProduct(Product body);

    Mono<Void> deleteProductById(int productId);
}
