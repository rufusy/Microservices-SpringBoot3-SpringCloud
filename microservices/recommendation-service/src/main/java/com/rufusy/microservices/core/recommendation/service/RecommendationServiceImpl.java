package com.rufusy.microservices.core.recommendation.service;

import com.rufusy.microservices.api.core.recommendation.Recommendation;
import com.rufusy.microservices.api.exceptions.InvalidInputException;
import com.rufusy.microservices.core.recommendation.mapper.RecommendationMapper;
import com.rufusy.microservices.core.recommendation.persistence.RecommendationEntity;
import com.rufusy.microservices.core.recommendation.persistence.RecommendationRepository;
import com.rufusy.microservices.util.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public List<Recommendation> getRecommendations(int productId) {
        log.debug("getRecommendations: fetching recommendations for productId: {}", productId);

        if (productId < 1) {
            log.debug("getRecommendations: Invalid productId: {}", productId);
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<RecommendationEntity> entityList = repository.findByProductId(productId);
        List<Recommendation> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        log.debug("getRecommendations: response size: {}", list.size());
        return list;
    }

    @Transactional
    @Override
    public Recommendation createRecommendation(Recommendation body) {
        RecommendationEntity newEntity = repository.save(mapper.apiToEntity(body));
        log.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
        return mapper.entityToApi(newEntity);
    }

    @Transactional
    @Override
    public boolean deleteRecommendations(int productId) {
        if (productId < 1) {
            log.debug("Invalid productId: {}", productId);
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
        return true;
    }
}
