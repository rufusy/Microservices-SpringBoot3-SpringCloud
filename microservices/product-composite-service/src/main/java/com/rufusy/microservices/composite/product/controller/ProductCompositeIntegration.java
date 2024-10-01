package com.rufusy.microservices.composite.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rufusy.microservices.api.core.product.Product;
import com.rufusy.microservices.api.core.product.ProductResource;
import com.rufusy.microservices.api.core.recommendation.Recommendation;
import com.rufusy.microservices.api.core.recommendation.RecommendationResource;
import com.rufusy.microservices.api.core.review.Review;
import com.rufusy.microservices.api.core.review.ReviewResource;
import com.rufusy.microservices.api.event.Event;
import com.rufusy.microservices.api.exceptions.EventProcessingException;
import com.rufusy.microservices.api.exceptions.InvalidInputException;
import com.rufusy.microservices.api.exceptions.NotFoundException;
import com.rufusy.microservices.util.HttpErrorInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.Objects;

import static com.rufusy.microservices.api.event.Event.Type.CREATE;
import static com.rufusy.microservices.api.event.Event.Type.DELETE;
import static java.util.logging.Level.FINE;

@Slf4j
@Component
public class ProductCompositeIntegration implements ProductResource, RecommendationResource, ReviewResource {
    private final ObjectMapper mapper;
    private final StreamBridge streamBridge;
    private final Scheduler publishEventScheduler;
    private final WebClient webClient;

    private static final String PRODUCT_SERVICE_URL = "http://product";
    private static final String RECOMMENDATION_SERVICE_URL = "http://recommendation";
    private static final String REVIEW_SERVICE_URL = "http://review";

    @Autowired
    public ProductCompositeIntegration(
            ObjectMapper mapper,
            StreamBridge streamBridge,
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            WebClient.Builder webClientBuilder) {

        this.mapper = mapper;
        this.streamBridge = streamBridge;
        this.publishEventScheduler = publishEventScheduler;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Product> getProduct(int productId) {

        String url = PRODUCT_SERVICE_URL + "/product/" + productId;

        log.debug("Will call getProduct API on URL: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log(log.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        return Mono.fromCallable(() -> {
            try {
                sendMessage("products-out-0", new Event<>(CREATE, body.getProductId(), body));
                return body;
            } catch (MessageDeliveryException ex) {
                log.warn("Failed to send {} event for productId: {}. Error message: {}", CREATE, body.getProductId(), ex.getMessage());
                throw new EventProcessingException(ex.getMessage());
            }
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        return Mono.fromRunnable(() -> {
            try {
                sendMessage("products-out-0", new Event<>(DELETE, productId, null));
            } catch (MessageDeliveryException ex) {
                log.warn("Failed to send {} event for productId: {}. Error message: {}", DELETE, productId, ex.getMessage());
                throw new EventProcessingException(ex.getMessage());
            }
        }).subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        String url = RECOMMENDATION_SERVICE_URL + "/recommendation?productId=" + productId;

        log.debug("Will call getRecommendations API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log(log.getName(), FINE)
                .onErrorResume(error -> Flux.empty());
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        return Mono.fromCallable(() -> {
            try {
                sendMessage("recommendations-out-0", new Event<>(CREATE, body.getProductId(), body));
                return body;
            } catch (MessageDeliveryException ex) {
                log.warn("Failed to send {} event for recommendations under productId: {}. Error message: {}", CREATE, body.getProductId(), ex.getMessage());
                throw new EventProcessingException(ex.getMessage());
            }
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        return Mono.fromRunnable(() -> {
            try {
                sendMessage("recommendations-out-0", new Event<>(DELETE, productId, null));
            } catch (MessageDeliveryException ex) {
                log.warn("Failed to send {} event for recommendations under productId: {}. Error message: {}", DELETE, productId, ex.getMessage());
                throw new EventProcessingException(ex.getMessage());
            }
        }).subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        String url = REVIEW_SERVICE_URL + "/review?productId=" + productId;

        log.debug("Will call getReviews API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .log(log.getName(), FINE)
                .onErrorResume(error -> Flux.empty());
    }

    @Override
    public Mono<Review> createReview(Review body) {
        return Mono.fromCallable(() -> {
            try {
                sendMessage("reviews-out-0", new Event<>(CREATE, body.getProductId(), body));
                return body;
            } catch (MessageDeliveryException ex) {
                log.warn("Failed to send {} event for reviews under productId: {}. Error message: {}", CREATE, body.getProductId(), ex.getMessage());
                throw new EventProcessingException(ex.getMessage());
            }
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteReview(int productId) {
        return Mono.fromRunnable(() -> {
            try {
                sendMessage("reviews-out-0", new Event<>(DELETE, productId, null));
            } catch (MessageDeliveryException ex) {
                log.warn("Failed to send {} event for reviews under productId: {}. Error message: {}", DELETE, productId, ex.getMessage());
                throw new EventProcessingException(ex.getMessage());
            }
        }).subscribeOn(publishEventScheduler).then();
    }

    private void sendMessage(String bindingName, Event<?, ?> event) {
        log.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message<? extends Event<?, ?>> message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException wcre)) {
            log.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.toString());
            return ex;
        }

        switch (Objects.requireNonNull(HttpStatus.resolve(wcre.getStatusCode().value()))) {
            case NOT_FOUND -> {
                return new NotFoundException(getErrorMessage(wcre));
            }
            case UNPROCESSABLE_ENTITY -> {
                return new InvalidInputException(getErrorMessage(wcre));
            }
            default -> {
                log.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                log.warn("Error body: {}", wcre.getResponseBodyAsString());
                return wcre;
            }
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException e) {
            return ex.getMessage();
        }
    }
}
