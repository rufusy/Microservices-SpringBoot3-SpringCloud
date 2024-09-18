package com.rufusy.microservices.core.product;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@ComponentScan("com.rufusy")
public class ProductServiceApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ProductServiceApplication.class, args);

		String mongodbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
		String mongodbPort =  ctx.getEnvironment().getProperty("spring.data.mongodb.port");
		log.info("Connected to MongoDb: {} : {}", mongodbHost, mongodbPort);
	}
}
