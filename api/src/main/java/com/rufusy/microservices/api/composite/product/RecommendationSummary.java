package com.rufusy.microservices.api.composite.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Getter
public class RecommendationSummary {
    private final int recommendationId;
    private final String author;
    private final int rate;
}
