package io.github.bubalehich.events.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HelloWorldRunner implements CommandLineRunner {

	@Override
	public void run(String... args) {
		log.info("hello world");
	}

}
