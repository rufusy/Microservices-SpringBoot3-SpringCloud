package com.rufusy.microservices.composite.product.controller;

import com.rufusy.microservices.api.composite.product.*;
import com.rufusy.microservices.api.core.product.Product;
import com.rufusy.microservices.api.core.recommendation.Recommendation;
import com.rufusy.microservices.api.core.review.Review;
import com.rufusy.microservices.api.exceptions.NotFoundException;
import com.rufusy.microservices.util.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
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
        log.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);

        Product product = integration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        List<Recommendation> recommendations = integration.getRecommendations(productId);

        List<Review> reviews = integration.getReviews(productId);

        log.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);

        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public ProductAggregate createProduct(ProductAggregate body) {
        try {
            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(product);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(),
                            r.getAuthor(), r.getRate(), r.getContent(), null);
                    integration.createRecommendation(recommendation);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(),
                            r.getContent(), null);
                    integration.createReview(review);
                });
            }

            return null;

        } catch (RuntimeException re) {
            log.warn("createCompositeProduct failed", re);
            throw re;
        }
    }

    @Override
    public void deleteProduct(int productId) {
        log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

        integration.deleteProduct(productId);

        integration.deleteRecommendations(productId);

        integration.deleteReview(productId);
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
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null : recommendations.stream()
                .map(r -> RecommendationSummary.builder()
                        .recommendationId(r.getRecommendationId())
                        .author(r.getAuthor())
                        .rate(r.getRate())
                        .content(r.getContent())
                        .build()
                ).toList();

        // copy summary review info, if present
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null : reviews.stream()
                .map(r -> ReviewSummary.builder()
                        .reviewId(r.getReviewId())
                        .author(r.getAuthor())
                        .subject(r.getSubject())
                        .content(r.getContent())
                        .build()
                ).toList();

        // Create info regarding the involved microservices
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return ProductAggregate.builder()
                .productId(productId)
                .name(name)
                .weight(weight)
                .recommendations(recommendationSummaries)
                .reviews(reviewSummaries)
                .serviceAddresses(serviceAddresses)
                .build();
    }
}
