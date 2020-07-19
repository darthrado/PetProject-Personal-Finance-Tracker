package com.ryanev.personalfinancetracker.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Requested Id was not found")
public class IncorrectMovementIdException extends Exception {
}
