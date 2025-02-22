package com.tianle_cinemaScrapper.cinemaScrapper;

import com.tianle_cinemaScrapper.cinemaScrapper.service.ScraperService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CinemaScrapperApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().load();
		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		System.setProperty("MONGODB_URI", dotenv.get("MONGODB_URI"));

		SpringApplication.run(CinemaScrapperApplication.class, args);
	}

	@Bean
	CommandLineRunner run(ScraperService scraperService) {
		return args -> {
			System.out.println("==========[Program start]==========");
			scraperService.startScraping();
			System.out.println("==========[Program finish]=========");
		};
	}

}
