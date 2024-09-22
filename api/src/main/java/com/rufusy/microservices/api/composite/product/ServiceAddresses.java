package com.rufusy.microservices.api.composite.product;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServiceAddresses {
    private String cmp;
    private String pro;
    private String rev;
    private String rec;
}
