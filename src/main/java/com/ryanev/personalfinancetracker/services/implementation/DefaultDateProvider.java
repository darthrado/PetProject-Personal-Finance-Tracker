package com.ryanev.personalfinancetracker.services.implementation;

import com.ryanev.personalfinancetracker.services.DateProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DefaultDateProvider implements DateProvider {
    @Override
    public LocalDate getNow() {
        return LocalDate.now();
    }
}
