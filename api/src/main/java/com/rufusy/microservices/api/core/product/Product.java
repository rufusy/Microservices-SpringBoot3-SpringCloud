package com.rufusy.microservices.api.core.product;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Product {
    private int productId;
    private String name;
    private int weight;
    private String serviceAddress;
}
