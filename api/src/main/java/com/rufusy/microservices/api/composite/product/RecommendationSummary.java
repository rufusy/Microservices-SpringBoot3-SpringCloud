package com.rufusy.microservices.api.composite.product;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecommendationSummary {
    private int recommendationId;
    private String author;
    private String content;
    private int rate;
}
