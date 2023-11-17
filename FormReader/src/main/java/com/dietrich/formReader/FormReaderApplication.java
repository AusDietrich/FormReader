package com.dietrich.formReader;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.dietrich.formReader.service.FileStorage;

import jakarta.annotation.Resource;

@SpringBootApplication
public class FormReaderApplication implements CommandLineRunner {

	@Resource
	FileStorage storageService;

	public static void main(String[] args) {
		SpringApplication.run(FormReaderApplication.class, args);
	}

	  @Override
	  public void run(String... arg) throws Exception {
//	    storageService.deleteAll();
	    storageService.init();
	  }
}
