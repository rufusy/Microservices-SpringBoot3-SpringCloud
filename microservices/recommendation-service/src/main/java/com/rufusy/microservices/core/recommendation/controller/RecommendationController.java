package com.rufusy.microservices.core.recommendation.controller;

import com.rufusy.microservices.api.core.recommendation.Recommendation;
import com.rufusy.microservices.api.core.recommendation.RecommendationResource;
import com.rufusy.microservices.api.exceptions.InvalidInputException;
import com.rufusy.microservices.core.recommendation.service.RecommendationService;
import com.rufusy.microservices.util.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class RecommendationController implements RecommendationResource {
    private final RecommendationService service;

    @Autowired
    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        return service.getRecommendations(productId);
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        return service.createRecommendation(body);
    }

    @Override
    public void deleteRecommendations(int productId) {
        service.deleteRecommendations(productId);
    }
}
