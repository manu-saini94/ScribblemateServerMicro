package com.scribblemate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AuthsMicroApplication {
	public static void main(String[] args) {
		SpringApplication.run(AuthsMicroApplication.class, args);
	}

}
