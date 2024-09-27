package com.rufusy.microservices.core.recommendation.service;

import com.rufusy.microservices.api.core.recommendation.Recommendation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RecommendationService {
    Mono<Recommendation> createRecommendation(Recommendation body);

    Flux<Recommendation> getRecommendations(int productId);

    Mono<Void> deleteRecommendations(int productId);
}
