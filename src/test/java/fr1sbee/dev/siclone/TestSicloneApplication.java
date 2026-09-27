package fr1sbee.dev.siclone;

import org.springframework.boot.SpringApplication;

public class TestSicloneApplication {

	public static void main(String[] args) {
		SpringApplication.from(SicloneApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
