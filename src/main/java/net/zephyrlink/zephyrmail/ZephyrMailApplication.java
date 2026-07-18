package net.zephyrlink.zephyrmail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZephyrMailApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZephyrMailApplication.class, args);
	}

}
