package com.loan.decisionengine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LoanDecisionException extends RuntimeException {
    public LoanDecisionException(String message) {
        super(message);
    }
}
