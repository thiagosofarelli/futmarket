package com.ar.edu.unq.futmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class FutmarketApplication {

	public static void main(String[] args) {
		java.util.TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
		SpringApplication.run(FutmarketApplication.class, args);
	}

}
