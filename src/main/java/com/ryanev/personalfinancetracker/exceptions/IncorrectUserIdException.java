package com.ryanev.personalfinancetracker.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "Incorrect User Id")
public class IncorrectUserIdException extends Exception {
}
