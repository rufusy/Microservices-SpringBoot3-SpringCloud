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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.rufusy.microservices.api.event.Event.Type.CREATE;
import static com.rufusy.microservices.api.event.Event.Type.DELETE;
import static org.springframework.http.HttpMethod.GET;

@Slf4j
@Component
public class ProductCompositeIntegration implements ProductResource, RecommendationResource, ReviewResource {
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    private final StreamBridge streamBridge;

    @Autowired
    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper mapper,
            StreamBridge streamBridge,

            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") String productServicePort,

            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") String recommendationServicePort,

            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") String reviewServicePort) {

        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.streamBridge = streamBridge;

        productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
    }

    @Override
    public Product getProduct(int productId) {
        try {
            String url = productServiceUrl + "/" + productId;
            log.debug("Will call getProduct API on URL: {}", url);

            Product product = restTemplate.getForObject(url, Product.class);
            assert product != null;
            log.debug("Found a product with id: {}", product.getProductId());

            return product;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientErrorException(ex);
        }
    }

    @Override
    public Product createProduct(Product body) {
        try {
            sendMessage("products-out-0", new Event<>(CREATE, body.getProductId(), body));
            return body;
        } catch (MessageDeliveryException ex) {
            log.warn("Failed to send {} event for productId: {}. Error message: {}", CREATE, body.getProductId(), ex.getMessage());
            throw handleMessageException(ex);
        }
    }

    @Override
    public void deleteProduct(int productId) {
        try {
            sendMessage("products-out-0", new Event<>(DELETE, productId, null));
        }catch (MessageDeliveryException ex) {
            log.warn("Failed to send {} event for productId: {}. Error message: {}", DELETE, productId, ex.getMessage());
            throw handleMessageException(ex);
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + "?productId=" + productId;

            log.debug("Will call getRecommendations API on URL: {}", url);
            List<Recommendation> recommendations = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {
                    })
                    .getBody();

            assert recommendations != null;
            log.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
            return recommendations;

        } catch (Exception ex) {
            log.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try {
            sendMessage("recommendations-out-0", new Event<>(CREATE, body.getProductId(), body));
            return body;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientErrorException(ex);
        }
    }

    @Override
    public void deleteRecommendations(int productId) {
        try {
            sendMessage("recommendations-out-0", new Event<>(DELETE, productId, null));
        } catch (MessageDeliveryException ex) {
            log.warn("Failed to send {} event for productId: {}. Error message: {}", DELETE, productId, ex.getMessage());
            throw handleMessageException(ex);
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + "?productId=" + productId;

            log.debug("Will call getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {
                    })
                    .getBody();

            assert reviews != null;
            log.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
            return reviews;
        } catch (Exception ex) {
            log.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Review createReview(Review body) {
        try {
            sendMessage("reviews-out-0", new Event<>(CREATE, body.getProductId(), body));
            return body;
        } catch (MessageDeliveryException ex) {
            log.warn("Failed to send {} event for productId: {}. Error message: {}", CREATE, body.getProductId(), ex.getMessage());
            throw handleMessageException(ex);
        }
    }

    @Override
    public void deleteReview(int productId) {
        try {
            sendMessage("reviews-out-0", new Event<>(DELETE, productId, null));
        } catch (MessageDeliveryException ex) {
            log.warn("Failed to send {} event for productId: {}. Error message: {}", DELETE, productId, ex.getMessage());
            throw handleMessageException(ex);
        }
    }

    private void sendMessage(String bindingName, Event<?, ?> event) {
        log.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message<? extends Event<?, ?>> message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

    private RuntimeException handleHttpClientErrorException(HttpClientErrorException ex) {
        switch (Objects.requireNonNull(HttpStatus.resolve(ex.getStatusCode().value()))) {
            case NOT_FOUND -> {
                return new NotFoundException(getErrorMessage(ex));
            }
            case UNPROCESSABLE_ENTITY -> {
                return new InvalidInputException(getErrorMessage(ex));
            }
            default -> {
                log.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                log.warn("Error body: {}", ex.getResponseBodyAsString());
                return ex;
            }
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException e) {
            return ex.getMessage();
        }
    }

    private RuntimeException handleMessageException(MessageDeliveryException ex) {
        return new EventProcessingException(ex.getMessage());
    }
}
