package com.example.productsreview.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class DotenvConfig {

    @PostConstruct
    public static void loadDotenv() {
        Dotenv dotenv = Dotenv.configure().load();
        System.setProperty("MONGO_INITDB_ROOT_USERNAME", Objects.requireNonNull(dotenv.get("MONGO_INITDB_ROOT_USERNAME")));
        System.setProperty("MONGO_INITDB_ROOT_PASSWORD", Objects.requireNonNull(dotenv.get("MONGO_INITDB_ROOT_PASSWORD")));
        System.setProperty("SPRING_DATA_MONGODB_USERNAME", Objects.requireNonNull(dotenv.get("SPRING_DATA_MONGODB_USERNAME")));
        System.setProperty("SPRING_DATA_MONGODB_PASSWORD", Objects.requireNonNull(dotenv.get("SPRING_DATA_MONGODB_PASSWORD")));
        System.setProperty("SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE", Objects.requireNonNull(dotenv.get("SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE")));
        System.setProperty("SPRING_DATA_MONGODB_HOST", Objects.requireNonNull(dotenv.get("SPRING_DATA_MONGODB_HOST")));
        System.setProperty("SPRING_DATA_MONGODB_PORT", Objects.requireNonNull(dotenv.get("SPRING_DATA_MONGODB_PORT")));
        System.setProperty("SPRING_DATA_MONGODB_DATABASE", Objects.requireNonNull(dotenv.get("SPRING_DATA_MONGODB_DATABASE")));
        System.setProperty("SPRING_DATA_MONGODB_AUTO_INDEX_CREATION", Objects.requireNonNull(dotenv.get("SPRING_DATA_MONGODB_AUTO_INDEX_CREATION")));
    }
}
