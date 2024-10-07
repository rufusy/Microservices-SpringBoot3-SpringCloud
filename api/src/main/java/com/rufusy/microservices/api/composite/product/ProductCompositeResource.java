package com.rufusy.microservices.api.composite.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@SecurityRequirement(name = "security_auth")
@Tag(name = "ProductComposite", description = "REST API for composite product information")
public interface ProductCompositeResource {
    /**
     * Sample usage:
     * curl $HOST:PORT/product-composite/1
     *
     * @param productId id of the product
     * @return the composite product if found, else null
     */
    @Operation(
            summary = "${api.product-composite.get-composite-product.description}",
            description = "${api.product-composite.get-composite-product.notes}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @GetMapping(
            value = "/product-composite/{productId}",
            produces = "application/json")
    Mono<ProductAggregate> getProduct(@PathVariable int productId);

    /**
     * Sample usage
     * curl -X POST $HOST:$PORT/product-composite \
     * -H "Content-Type: application/json" \
     * -d '{"productId":1,"name":"name","weight":5}'
     *
     * @param body new composite product
     * @return A JSON representation of the newly created composite product
     */
    @Operation(
            summary = "${api.product-composite.create-composite-product.description}",
            description = "${api.product-composite.create-composite-product.notes}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(
            value = "/product-composite",
            consumes = "application/json",
            produces = "application/json")
    Mono<Void> createProduct(@RequestBody ProductAggregate body);

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/product-composite/1"
     *
     * @param productId id of the product
     */
    @Operation(
            summary = "${api.product-composite.delete-composite-product.description}",
            description = "${api.product-composite.delete-composite-product.notes}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping(value = "/product-composite/{productId}")
    Mono<Void> deleteProduct(@PathVariable int productId);
}
