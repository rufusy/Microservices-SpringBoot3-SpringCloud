package com.rufusy.microservices.core.recommendation.controller;

import com.rufusy.microservices.api.core.recommendation.Recommendation;
import com.rufusy.microservices.api.core.recommendation.RecommendationResource;
import com.rufusy.microservices.core.recommendation.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class RecommendationController implements RecommendationResource {
    private final RecommendationService service;

    @Autowired
    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        return service.getRecommendations(productId);
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        return service.createRecommendation(body);
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        return service.deleteRecommendations(productId);
    }
}
