package statutesrestca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"statutes", "statutesca", "gsearch"})
public class StatutesRestCaApplication {

	public static void main(String[] args) {
		SpringApplication.run(StatutesRestCaApplication.class, args);
	}

}