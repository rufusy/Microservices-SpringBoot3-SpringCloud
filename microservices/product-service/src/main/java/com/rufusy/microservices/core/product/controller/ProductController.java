package com.rufusy.microservices.core.product.controller;

import com.rufusy.microservices.api.core.product.Product;
import com.rufusy.microservices.api.core.product.ProductResource;
import com.rufusy.microservices.core.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ProductController implements ProductResource {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public Product getProduct(int productId) {
        return productService.getProduct(productId);
    }

    @Override
    public Product createProduct(Product body) {
        return productService.createProduct(body);
    }

    @Override
    public void deleteProduct(int productId) {
        productService.deleteProductById(productId);
    }
}
