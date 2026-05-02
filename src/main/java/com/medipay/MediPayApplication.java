package com.medipay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MediPayApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediPayApplication.class, args);
	}

}
