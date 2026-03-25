package com.pharmacy.pharmacy_system;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class PharmacySystemApplication {

	@PostConstruct
	public void init() {
		// Set default timezone to Karachi (PKT)
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Karachi"));
	}

	public static void main(String[] args) {
		SpringApplication.run(PharmacySystemApplication.class, args);
	}

}
