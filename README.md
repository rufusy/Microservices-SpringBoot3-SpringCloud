# Reactive Microservices with Spring Boot, Docker, and Kubernetes

This is a demo project showcasing how to build a set of reactive microservices using Spring Boot 3, Spring Cloud, 
Docker, and Kubernetes. 
The project uses non-blocking synchronous communication with Spring WebFlux and event-driven asynchronous communication
with RabbitMQ/Kafka for inter-service interactions.
The application includes OpenAPI-based documentation for the external API, which is exposed through an edge server. 
It also provides a Swagger UI viewer for visualizing and testing the APIs.

## Microservices
The focus of this project is on demonstrating the modules and design patterns required to build microservices. 
As a result, the business logic within the services is intentionally kept minimal.

### The product service
The product service manages product information and describes each product with the following
attributes:
- Product ID: This is not the primary key (PK) but rather a unique key that identifies a product in storage
- Name
- Weight

### The review service
The review service manages product reviews and stores the following information about each 
review:
- Product ID
- Review ID: This is not the primary key (PK)
- Author
- Subject
- Content

Product ID and Review ID together form a compound key that uniquely identifies a review in storage.

### The recommendation service
The recommendation service manages product recommendations and stores the following information
about each recommendation:
- Product ID
- Recommendation ID: This is not the primary key (PK)
- Author
- Rate
- Content

Product ID and Recommendation ID together form a compound key that uniquely identifies a recommendation in storage.

### The product composite service
The product composite service aggregates information from the three core services and presents
information about a product as follows:
- Product information, as described in the product service
- A list of product reviews for the specified product, as described in the review service
- A list of product recommendations for the specified product, as described in the recommendation
  service

## Other projects
### API
This project is packaged as a library. It contains:
- Interfaces that define the RESTful APIs.
- Model classes (DTOs) used in API requests and responses.
- Exceptions returned by the API.
- Event types that can be sent to a message broker.

### Util
Also packaged as a library, this project contains helper classes shared by the microservices. For example, it includes 
utilities for handling errors consistently and for returning the address of each contacted microservice.

## Spring Cloud
The following Spring Cloud components have been used to implement various design patterns

| Design Pattern             | Component                                      |
|----------------------------|------------------------------------------------|
| Service discovery          | Netflix Eureka and Spring Cloud LoadBalancer   |
| Edge server                | Spring Cloud Gateway and Spring Security OAuth |
| Centralized configuration  | Spring Cloud Configuration Server              |
| Circuit breaker            | Resilience4j                                   |
| Distributed tracing        | Micrometer Tracing and Zipkin                  |
| Event driven communication | Spring cloud stream                            |


## Security

The following measures have been implemented to secure the services:

- **HTTPS Encryption**:
  - All external requests and responses to the external API are encrypted using HTTPS with a self-signed certificate.
  - The certificate is specified at runtime when using Docker and added to the classpath outside of Docker.
  - Note that HTTPS is applied only to the edge server; plain HTTP is used internally.

- **Basic Authentication**:
  - The discovery server (Netflix Eureka) requires basic authentication with the credentials: `user/pwd`.

- **OAuth 2.0 and OIDC**:
  - Users and client applications accessing the APIs are authenticated and authorized using OAuth 2.0 and OIDC.
  - The edge server and product composite service act as resource servers.
  - For local development and testing, Spring Authorization Server is used. A resource owner will have credentials: `user/pwd`.

- **Client Credentials**:
  - **Client 1 (Writer)**:
    - Credentials: `writer/secret-writer`.
    - Scopes:
      - `product:write`
      - `product:read`
  - **Client 2 (Reader)**:
    - Credentials: `reader/secret-reader`.
    - Scope: `product:read`.

- **Swagger UI Configuration**:
  - The Swagger UI component is configured to identify as the writer client and can be authorized with the writer's scopes to interact with the API.

## Testing
The project can be configured to work with either RabbitMQ or Kafka:
- To use RabbitMQ without partitions, refer to the docker-compose.yaml file.
- To use RabbitMQ with partitions, refer to the docker-compose-partitions.yaml file.
- To use Kafka, refer to the docker-compose-kafka.yaml file.

