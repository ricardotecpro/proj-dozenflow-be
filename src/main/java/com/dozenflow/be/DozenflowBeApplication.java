package com.dozenflow.be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import java.util.Collections;

// UserDetailsServiceAutoConfiguration is excluded because every endpoint is permitAll
// (see SecurityConfig) — without this, Spring Boot still stands up a default in-memory
// user with a random password logged on every boot ("Using generated security password"),
// which protects nothing here and just adds noise/confusion to the logs.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class DozenflowBeApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(DozenflowBeApplication.class);
		// Programmatically set the default profile to 'dev'.
		// This is the most robust way to ensure the dev profile is active for local development,
		// independent of IDE configurations or launch scripts. It only applies if no
		// other profile is specified via properties or command line.
		app.setDefaultProperties(Collections.singletonMap("spring.profiles.default", "dev"));
		app.run(args);
	}

}