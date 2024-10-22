package com.rufusy.microservices.composite.product.controller;

import com.rufusy.microservices.api.composite.product.*;
import com.rufusy.microservices.api.core.product.Product;
import com.rufusy.microservices.api.core.recommendation.Recommendation;
import com.rufusy.microservices.api.core.review.Review;
import com.rufusy.microservices.util.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

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
    public Mono<ProductAggregate> getProduct(int productId, int delay, int faultPercent) {
        log.info("Will get composite product info for product.id={}", productId);

        return Mono.zip(values -> createProductAggregate(
                (Product) values[0], (List<Recommendation>) values[1], (List<Review>) values[2], serviceUtil.getServiceAddress()
                        ),
                        integration.getProduct(productId, delay, faultPercent),
                        integration.getRecommendations(productId).cast(Recommendation.class).collectList(),
                        integration.getReviews(productId).cast(Review.class).collectList())
                .doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
                .log(log.getName(), Level.FINE);
    }

    @Override
    public Mono<Void> createProduct(ProductAggregate body) {
        try {
            List<Mono<?>> monoList = new ArrayList<>();

            log.info("Will create a new composite entity for product.id: {}", body.getProductId());

            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            monoList.add(integration.createProduct(product));

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(),
                            r.getAuthor(), r.getRate(), r.getContent(), null);

                    monoList.add(integration.createRecommendation(recommendation));
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(),
                            r.getContent(), null);

                    monoList.add(integration.createReview(review));
                });
            }

            log.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

            return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
                    .doOnError(ex -> log.warn("createCompositeProduct failed: {}", ex.toString()))
                    .then();

        } catch (RuntimeException re) {
            log.warn("createCompositeProduct failed", re);
            throw re;
        }
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        try {

            log.info("Will delete a product aggregate for product.id: {}", productId);

            return Mono.zip(r -> "",
                            integration.deleteProduct(productId),
                            integration.deleteRecommendations(productId),
                            integration.deleteReview(productId))
                    .doOnError(ex -> log.warn("delete failed: {}", ex.toString()))
                    .log(log.getName(), Level.FINE)
                    .then();

        } catch (RuntimeException re) {
            log.warn("deleteCompositeProduct failed: {}", re.toString());
            throw re;
        }
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
