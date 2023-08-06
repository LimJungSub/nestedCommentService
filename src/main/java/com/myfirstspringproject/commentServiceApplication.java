package com.myfirstspringproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
//import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
//import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
//@ConfigurationPropertiesScan
//@EntityScan

@EnableJpaAuditing //위치? 설정파일이라는데
public class commentServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(commentServiceApplication.class, args);
	}

}


