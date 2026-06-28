package com.loan.decisionengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LoanDecisionApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoanDecisionApplication.class, args);
    }
}
