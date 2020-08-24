package com.ryanev.personalfinancetracker.services.util;

import com.ryanev.personalfinancetracker.services.util.DateProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DefaultDateProvider implements DateProvider {
    @Override
    public LocalDate getNow() {
        return LocalDate.now();
    }
}
