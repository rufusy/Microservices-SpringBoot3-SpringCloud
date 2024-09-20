package com.rufusy.microservices.api.core.recommendation;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface RecommendationResource {
    /**
     * Sample usage:
     * curl $HOST:$PORT/recommendation?productId=1
     *
     * @param productId id of the product
     * @return Recommendations of the product
     */
    @GetMapping(
            value = "/recommendation",
            produces = "application/json")
    List<Recommendation> getRecommendations(@RequestParam(value = "productId") int productId);

    /**
     * Sample usage:
     * curl -X POST $HOST:$PORT/recommendation \
     * -H "Content-Type: application/json" \
     * -d '{"productId":123, "recommendationId":456, "author":"author", "rate":5, "content":"content"}'
     *
     * @param body new recommendation
     * @return newly created recommendation
     */
    @PostMapping(
            value = "/recommendation",
            consumes = "application/json",
            produces = "application/json")
    Recommendation createRecommendation(@RequestBody Recommendation body);

    /**
     * Sample usage:
     * curl -X DELETE $HOST:$PORT/recommendation?productId=1
     * @param productId id of the product
     */
    @DeleteMapping(value = "/recommendation")
    void deleteRecommendations(@RequestParam(value = "productId") int productId );
}
