package com.rufusy.microservices.api.core.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Getter
public class Product {
    private final int productId;
    private final String name;
    private final int weight;
    private final String serviceAddress;
}
