package com.codesync.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║   CodeSync API Gateway Started!                  ║");
        System.out.println("║   Port: 8080                                     ║");
        System.out.println("║   Routes all /api/v1/* to microservices          ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");
    }
}
