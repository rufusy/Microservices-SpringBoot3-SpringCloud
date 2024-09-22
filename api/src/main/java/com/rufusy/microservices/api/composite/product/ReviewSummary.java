package com.rufusy.microservices.api.composite.product;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ReviewSummary {
    private int reviewId;
    private String author;
    private String subject;
    private String content;
}
