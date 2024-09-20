package com.rufusy.microservices.core.recommendation.controller;

import com.rufusy.microservices.api.core.recommendation.Recommendation;
import com.rufusy.microservices.api.core.recommendation.RecommendationResource;
import com.rufusy.microservices.api.exceptions.InvalidInputException;
import com.rufusy.microservices.util.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class RecommendationController implements RecommendationResource {
    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationController(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if (productId == 113) {
            log.debug("No recommendation found for productId: {}", productId);
            return new ArrayList<>();
        }

        List<Recommendation> list = new ArrayList<>();
        list.add(new Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
        list.add(new Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
        list.add(new Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));

        log.debug("/recommendation response size: {}", list.size());

        return list;
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        return null;
    }

    @Override
    public void deleteRecommendations(int productId) {

    }
}
