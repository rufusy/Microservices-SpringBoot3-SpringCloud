package com.rufusy.microservices.core.review.config;

import com.rufusy.microservices.api.core.review.Review;
import com.rufusy.microservices.api.event.Event;
import com.rufusy.microservices.api.exceptions.EventProcessingException;
import com.rufusy.microservices.core.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class MessageProcessorConfig {
    private final ReviewService reviewService;

    public MessageProcessorConfig(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Bean
    public Consumer<Event<Integer, Review>> messageProcessor() {
        return event -> {
            log.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getEventType()) {
                case CREATE -> {
                    Review review = event.getData();
                    log.info("Create review with ID: {}/{}", review.getProductId(), review.getReviewId());
                    reviewService.createReview(review);
                }
                case DELETE -> {
                    int productId = event.getKey();
                    log.info("Delete review with ProductID: {}", productId);
                    reviewService.deleteReviews(productId);
                }
                default -> {
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                    log.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }

            log.info("Message processing done!");
        };
    }
}
