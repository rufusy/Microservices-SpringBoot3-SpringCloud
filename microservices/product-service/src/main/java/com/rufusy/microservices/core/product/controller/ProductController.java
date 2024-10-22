package com.rufusy.microservices.core.product.controller;

import com.rufusy.microservices.api.core.product.Product;
import com.rufusy.microservices.api.core.product.ProductResource;
import com.rufusy.microservices.core.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class ProductController implements ProductResource {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public Mono<Product> getProduct(int productId, int delay, int faultPercent) {
        return productService.getProduct(productId, delay, faultPercent);
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        return productService.createProduct(body);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        return productService.deleteProductById(productId);
    }
}
