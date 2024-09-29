package com.rufusy.microservices.composite.product;

import com.rufusy.microservices.composite.product.controller.ProductCompositeIntegration;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class HealthCheckConfiguration {

    ProductCompositeIntegration integration;

    public HealthCheckConfiguration(ProductCompositeIntegration integration) {
        this.integration = integration;
    }

    @Bean
    ReactiveHealthContributor coreServices() {
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

        registry.put("product", () -> integration.getProductHealth());
        registry.put("recommendation", () -> integration.getRecommendationHealth());
        registry.put("review", () -> integration.getReviewHealth());

        return CompositeReactiveHealthContributor.fromMap(registry);
    }
}
