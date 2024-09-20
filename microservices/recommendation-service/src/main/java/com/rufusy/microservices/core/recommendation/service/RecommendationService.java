package com.rufusy.microservices.core.recommendation.service;

import com.rufusy.microservices.api.core.recommendation.Recommendation;

import java.util.List;

public interface RecommendationService {
    Recommendation createRecommendation(Recommendation body);

    List<Recommendation> getRecommendations(int productId);

    boolean deleteRecommendations(int productId);
}
