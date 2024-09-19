package com.rufusy.microservices.api.core.review;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ReviewResource {
    /**
     * Sample usage:
     * curl $HOST:$PORT/review?productId=1
     *
     * @param productId id of the product
     * @return Reviews of the product
     */
    @GetMapping(
            value = "/review",
            produces = "application/json")
    List<Review> getReviews(@RequestParam(value = "productId") int productId);

    /**
     * Sample usage:
     * curl -X POST $HOST:$PORT/review \
     * -H "Content-Type: application/json" \
     * -d '{"productId":123, "reviewId":456, "author":"author", "subject":"subject", "content":"content"}'
     *
     * @param body new review
     * @return newly created review
     */
    @PostMapping(
            value = "/review",
            consumes = "application/json",
            produces = "application/json")
    Review createReview(@RequestBody Review body);

    /**
     * Sample usage:
     * curl -X DELETE $HOST:$PORT/review?productId=1
     *
     * @param productId id of the product
     */
    @DeleteMapping(value = "/review")
    void deleteReview(@RequestParam(value = "productId") int productId);
}
