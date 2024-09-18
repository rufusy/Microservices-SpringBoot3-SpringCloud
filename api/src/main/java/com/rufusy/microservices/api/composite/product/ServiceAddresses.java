package com.rufusy.microservices.api.composite.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Getter
public class ServiceAddresses {
    private final String cmp;
    private final String pro;
    private final String rev;
    private final String rec;
}
