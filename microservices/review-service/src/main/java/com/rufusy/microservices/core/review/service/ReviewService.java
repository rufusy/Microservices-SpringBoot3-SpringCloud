package com.rufusy.microservices.core.review.service;

import com.rufusy.microservices.api.core.review.Review;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {
    Mono<Review> createReview(Review body);

    Flux<Review> getReviews(int productId);

    Mono<Void> deleteReviews(int productId);
}
