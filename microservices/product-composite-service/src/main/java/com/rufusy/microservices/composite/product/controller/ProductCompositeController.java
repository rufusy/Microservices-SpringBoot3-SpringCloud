package com.rufusy.microservices.composite.product.controller;

import com.rufusy.microservices.api.composite.product.*;
import com.rufusy.microservices.api.core.product.Product;
import com.rufusy.microservices.api.core.recommendation.Recommendation;
import com.rufusy.microservices.api.core.review.Review;
import com.rufusy.microservices.api.exceptions.NotFoundException;
import com.rufusy.microservices.util.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductCompositeController implements ProductCompositeResource {
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeController(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public ProductAggregate getProduct(int productId) {
        Product product = integration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        List<Recommendation> recommendations = integration.getRecommendations(productId);

        List<Review> reviews = integration.getReviews(productId);

        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public ProductAggregate createProduct(ProductAggregate body) {
        return null;
    }

    @Override
    public void deleteProduct(int productId) {

    }

    private ProductAggregate createProductAggregate(
            Product product,
            List<Recommendation> recommendations,
            List<Review> reviews,
            String serviceAddress) {

        // setup product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        // copy summary recommendation info, if present
        List<RecommendationSummary> recommendationSummaries =
                (recommendations == null) ? null :
                        recommendations.stream()
                                .map(r -> new RecommendationSummary(
                                        r.getRecommendationId(), r.getAuthor(), r.getRate())
                                )
                                .toList();

        // copy summary review info, if present
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null : reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject())).toList();

        // Create info regarding the involved microservices
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
