package com.rufusy.microservices.core.review.service;

import com.rufusy.microservices.api.core.review.Review;

import java.util.List;

public interface ReviewService {
    Review createReview(Review body);

    List<Review> getReviews(int productId);

    void deleteReviews(int productId);
}
