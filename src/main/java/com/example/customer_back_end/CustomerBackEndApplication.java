package com.example.customer_back_end;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication

// @service, @controller, @configuration, @restcontroller, @component ...
@ComponentScan(basePackages = {
		"com.example.controller",
		"com.example.controller1",
		"com.example.security",
		"com.example.token",
		"com.example.restcontroller",
		"com.example.service",
		"com.example.config" })

// @mapper
@MapperScan(basePackages = {
		"com.example.mapper" })

// @entity
@EntityScan(basePackages = { "com.example.entity", "com.example.entity1" })

// @repository
@EnableJpaRepositories(basePackages = { "com.example.repository" })

public class CustomerBackEndApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(CustomerBackEndApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(CustomerBackEndApplication.class);
	}
}
