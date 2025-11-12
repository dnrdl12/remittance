package io.dnrdl12.remittance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RemittanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RemittanceApplication.class, args);
	}

}
