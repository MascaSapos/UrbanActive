package com.urbanactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class UrbanActiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrbanActiveApplication.class, args);
    }
}
