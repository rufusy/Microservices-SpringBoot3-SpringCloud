package com.rufusy.microservices.core.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
@SpringBootApplication
@ComponentScan("com.rufusy")
public class ReviewServiceApplication {
    private final Integer threadPoolSize;
    private final Integer taskQueueSize;

    public ReviewServiceApplication(
            @Value("${app.threadPoolSize:10}") Integer threadPoolSize,
            @Value("${app.taskQueueSize:100}") Integer taskQueueSize) {
        this.threadPoolSize = threadPoolSize;
        this.taskQueueSize = taskQueueSize;
    }

    @Bean
    public Scheduler jdbcScheduler() {
        log.info("Creates a jdbcScheduler with the thread pool size = {}", threadPoolSize);
        return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "jdbc-pool");
    }

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        ConfigurableApplicationContext ctx = SpringApplication.run(ReviewServiceApplication.class, args);
        String postgresUrl = ctx.getEnvironment().getProperty("spring.datasource.url");
        log.info("Connected to Postgres: {}", postgresUrl);
    }
}
