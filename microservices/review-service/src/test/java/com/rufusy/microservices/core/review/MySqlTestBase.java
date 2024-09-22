package com.rufusy.microservices.core.review;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

public abstract class MySqlTestBase {
    // Extend startup timeout since a MySQL container starts very slow
    private static JdbcDatabaseContainer<?> database = new MySQLContainer("mysql:8.0.32")
            .withConnectTimeoutSeconds(300);

    static {
        database.start();
    }

    @DynamicPropertySource
    static void setDatabaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
    }
}
