package com.rufusy.microservices.core.review.controller;

import com.rufusy.microservices.api.core.review.Review;
import com.rufusy.microservices.api.core.review.ReviewResource;
import com.rufusy.microservices.core.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class ReviewController implements ReviewResource {
    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        return service.getReviews(productId);
    }

    @Override
    public Mono<Review> createReview(Review body) {
        return service.createReview(body);
    }

    @Override
    public Mono<Void> deleteReview(int productId) {
        return service.deleteReviews(productId);
    }
}
