package com.example.productsreview;

import com.example.productsreview.config.DotenvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        DotenvConfig.loadDotenv();
        SpringApplication.run(Application.class, args);
    }

}
