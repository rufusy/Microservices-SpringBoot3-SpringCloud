package com.rufusy.microservices.core.product.service;

import com.rufusy.microservices.api.core.product.Product;

public interface ProductService {
    Product getProduct(int productId);

    Product createProduct(Product body);

    void deleteProductById(int productId);
}
