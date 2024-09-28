package com.rufusy.microservices.core.recommendation.service;

import com.rufusy.microservices.api.core.recommendation.Recommendation;
import com.rufusy.microservices.api.exceptions.InvalidInputException;
import com.rufusy.microservices.core.recommendation.mapper.RecommendationMapper;
import com.rufusy.microservices.core.recommendation.persistence.RecommendationEntity;
import com.rufusy.microservices.core.recommendation.persistence.RecommendationRepository;
import com.rufusy.microservices.util.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;

@Slf4j
@Service
public class RecommendationServiceImpl implements RecommendationService {
    private final RecommendationRepository repository;
    private final ServiceUtil serviceUtil;
    private final RecommendationMapper mapper;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository repository, ServiceUtil serviceUtil, RecommendationMapper mapper) {
        this.repository = repository;
        this.serviceUtil = serviceUtil;
        this.mapper = mapper;
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        log.debug("getRecommendations: fetching recommendations for productId: {}", productId);

        if (productId < 1) {
            log.debug("getRecommendations: Invalid productId: {}", productId);
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        
        return repository.findByProductId(productId)
                .log(log.getName(), FINE)
                .map(mapper::entityToApi)
                .map(e -> {
                    e.setServiceAddress(serviceUtil.getServiceAddress());
                    return e;
                });
    }

    @Transactional
    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        RecommendationEntity entity = mapper.apiToEntity(body);

        return repository.save(entity)
                .log(log.getName(), FINE)
                .onErrorMap(
                    DuplicateKeyException.class,
                    ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
                .map(mapper::entityToApi);
    }

    @Transactional
    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        if (productId < 1) {
            log.debug("Invalid productId: {}", productId);
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        return repository.deleteAll(repository.findByProductId(productId));
    }
}
