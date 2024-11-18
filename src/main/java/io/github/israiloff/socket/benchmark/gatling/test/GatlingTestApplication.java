package io.github.israiloff.socket.benchmark.gatling.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.github.israiloff.socket.benchmark.gatling.test")
public class GatlingTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatlingTestApplication.class, args);
	}

}
