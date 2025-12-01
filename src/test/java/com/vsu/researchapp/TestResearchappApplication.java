package com.vsu.researchapp;

import org.springframework.boot.SpringApplication;

public class TestResearchappApplication {

	public static void main(String[] args) {
		SpringApplication.from(ResearchappApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
