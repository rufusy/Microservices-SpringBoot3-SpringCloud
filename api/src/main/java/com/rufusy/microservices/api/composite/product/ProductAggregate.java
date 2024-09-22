package com.rufusy.microservices.api.composite.product;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductAggregate {
    private int productId;
    private String name;
    private int weight;
    private List<RecommendationSummary> recommendations;
    private List<ReviewSummary> reviews;
    private ServiceAddresses serviceAddresses;
}
