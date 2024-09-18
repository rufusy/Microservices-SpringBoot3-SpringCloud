package com.rufusy.microservices.api.composite.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Getter
public class ReviewSummary {
    private final int reviewId;
    private final String author;
    private final String subject;
}
