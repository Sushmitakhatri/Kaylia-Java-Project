package com.app.Kaylia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class KayliaApplication {

	public static void main(String[] args) {
		SpringApplication.run(KayliaApplication.class, args);
	}

}
