package de.proeller.applications.employeetest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
public class EmployeetestApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeetestApplication.class, args);
	}

}
