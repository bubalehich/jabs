package io.github.bubalehich.sports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ReactiveSportsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveSportsApplication.class, args);
	}

}
