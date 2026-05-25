package com.campusenroll.enrollment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EnrollmentServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(
				EnrollmentServiceApplication.class,
				args
		);

		String serverPort = System.getenv().getOrDefault("SERVER_PORT", "8085");

		System.out.printf("""
				
		========================================
		  ENROLLMENT-SERVICE INICIADO
		========================================
		
		URL:
		http://localhost:%s
		
		Base de datos:
		PostgreSQL Docker
		
		Estado:
		MICROSERVICIO FUNCIONANDO
		
		========================================
				
		""", serverPort);
	}
}
