package com.rufusy.microservices.api.core.review;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ReviewResource {

    @GetMapping(
            value = "/review",
            produces = "application/json")
    List<Review> getReviews(@RequestParam(value = "productId") int productId);
}
