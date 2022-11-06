package com.example.uploadingfiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.example.uploadingfiles.dao.RecordsRepository;
import com.example.uploadingfiles.storage.StorageProperties;
import com.example.uploadingfiles.storage.StorageService;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)

public class UploadingFilesApplication {

	public static void main(String[] args) {
		SpringApplication.run(UploadingFilesApplication.class, args);
	}
	
    @Autowired
    RecordsRepository recordsRepo;
	
	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			// deleting all existing tables in h2 database and initialize 
			storageService.deleteAll();
			storageService.init();
		};
	}
}
