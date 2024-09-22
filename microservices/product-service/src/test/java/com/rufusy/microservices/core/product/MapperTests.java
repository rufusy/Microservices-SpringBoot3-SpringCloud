package com.rufusy.microservices.core.product;

import com.rufusy.microservices.api.core.product.Product;
import com.rufusy.microservices.core.product.mapper.ProductMapper;
import com.rufusy.microservices.core.product.persistence.ProductEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

public class MapperTests {
    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(mapper);

        Product api = Product.builder()
                .productId(1).name("n").weight(1).serviceAddress("sa")
                .build();

        ProductEntity entity = mapper.apiToEntity(api);
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getWeight(), entity.getWeight());
        assertNull(entity.getVersion());
        assertNull(entity.getId());

        Product api2 = mapper.entityToApi(entity);
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getName(), api2.getName());
        assertEquals(api.getWeight(), api2.getWeight());
        assertNull(api2.getServiceAddress());
    }
}
