package com.rufusy.microservices.core.product;

import com.rufusy.microservices.core.product.persistence.ProductEntity;
import com.rufusy.microservices.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;

@DataMongoTest
public class PersistenceTests extends MongoTestBase {
    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;

    @BeforeEach
    void setupDb() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();

        ProductEntity entity = new ProductEntity(1, "n", 1);
        StepVerifier.create(repository.save(entity))
                .expectNextMatches(createdEntity -> {
                    savedEntity = createdEntity;
                    return areProductEqual(entity, savedEntity);
                })
                .verifyComplete();
    }

    @Test
    void create() {
        ProductEntity newEntity = new ProductEntity(2, "n", 2);

        StepVerifier.create(repository.save(newEntity))
                .expectNextMatches(createdEntity -> newEntity.getProductId() == createdEntity.getProductId())
                .verifyComplete();

        StepVerifier.create(repository.findById(newEntity.getId()))
                .expectNextMatches(foundEntity -> areProductEqual(newEntity, foundEntity))
                .verifyComplete();

        StepVerifier.create(repository.count()).expectNext(2L).verifyComplete();
    }

    @Test
    void update() {
        savedEntity.setName("n2");

        StepVerifier.create(repository.save(savedEntity))
                .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
                .verifyComplete();

        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity -> foundEntity.getVersion() == 1
                && foundEntity.getName().equals("n2"))
                .verifyComplete();
    }

    @Test
    void delete() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
        StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
    }

    @Test
    void getProductById() {
        StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
                        .expectNextMatches(foundEntity -> areProductEqual(savedEntity, foundEntity))
                                .verifyComplete();
    }

    //@Test
    // @todo re-enable
    void duplicateError() {
        ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
        StepVerifier.create(repository.save(entity))
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    void optimisticLockError() {
        // Store the saved entity in two separate entity objects
        ProductEntity entity1 = repository.findById(savedEntity.getId()).block();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        assert entity1 != null;
        entity1.setName("n1");
        repository.save(entity1).block();

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
        assert entity2 != null;
        StepVerifier.create(repository.save(entity2))
                .expectError(OptimisticLockingFailureException.class).verify();

        // Get the updated entity from the database and verify its new state
        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity ->
                        foundEntity.getVersion() == 1
                        && foundEntity.getName().equals("n1")
                ).verifyComplete();
    }

    private boolean areProductEqual(ProductEntity expectedEntity, ProductEntity actualEntity) {
        return (expectedEntity.getId().equals(actualEntity.getId()))
                && (expectedEntity.getVersion().equals(actualEntity.getVersion()))
                && (expectedEntity.getProductId() == actualEntity.getProductId())
                && (expectedEntity.getName().equals(actualEntity.getName()))
                && (expectedEntity.getWeight() == actualEntity.getWeight());
    }
}
