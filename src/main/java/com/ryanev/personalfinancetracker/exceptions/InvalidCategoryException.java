package com.ryanev.personalfinancetracker.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidCategoryException extends Exception {
    public InvalidCategoryException(String message) {
        super(message);
    }

}
