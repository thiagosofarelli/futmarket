package com.ar.edu.unq.futmarket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FutmarketApplication {

	public static void main(String[] args) {
		java.util.TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
		ensureAuditLogDirectoryExists();
		SpringApplication.run(FutmarketApplication.class, args);
	}

	private static void ensureAuditLogDirectoryExists() {
		try {
			Files.createDirectories(Path.of("logs"));
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to create audit log directory", exception);
		}
	}

}
