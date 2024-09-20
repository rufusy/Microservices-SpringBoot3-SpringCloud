package com.rufusy.microservices.core.product.service;

import com.rufusy.microservices.api.core.product.Product;
import com.rufusy.microservices.api.exceptions.InvalidInputException;
import com.rufusy.microservices.api.exceptions.NotFoundException;
import com.rufusy.microservices.core.product.mapper.ProductMapper;
import com.rufusy.microservices.core.product.persistence.ProductEntity;
import com.rufusy.microservices.core.product.persistence.ProductRepository;
import com.rufusy.microservices.util.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil, ProductRepository repository, ProductMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Product getProduct(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        ProductEntity entity = repository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

        Product response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());

        log.debug("getProduct: found productId: {}", productId);

        return response;
    }

    @Override
    public Product createProduct(Product body) {
        ProductEntity entity = mapper.apiToEntity(body);
        ProductEntity newEntity = repository.save(entity);

        log.debug("createProduct: entity created for productId: {}", body.getProductId());
        return mapper.entityToApi(newEntity);
    }

    @Override
    public void deleteProductById(int productId) {
        log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        repository.findByProductId(productId).ifPresent(repository::delete);
    }
}
