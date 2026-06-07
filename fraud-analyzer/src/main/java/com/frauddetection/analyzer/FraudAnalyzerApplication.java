package com.frauddetection.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class FraudAnalyzerApplication {
    public static void main(String[] args) {
        SpringApplication.run(FraudAnalyzerApplication.class, args);
    }
}