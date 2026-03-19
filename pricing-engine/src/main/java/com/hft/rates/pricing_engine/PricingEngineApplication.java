package com.hft.rates.pricing_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.hft.rates")
@EnableScheduling
public class PricingEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(PricingEngineApplication.class, args);
	}

}
