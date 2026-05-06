package com.codesync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class CodeSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeSyncApplication.class, args);
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║   CodeSync Backend Started Successfully!         ║");
        System.out.println("║   API:     http://localhost:8080/api/v1/         ║");
        System.out.println("║   Swagger: http://localhost:8080/swagger-ui.html ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");
    }
}
