package com.example.customer_back_end;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.controller"})
public class CustomerBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerBackEndApplication.class, args);
	}

}
