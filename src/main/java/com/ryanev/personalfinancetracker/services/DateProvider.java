package com.ryanev.personalfinancetracker.services;

import java.time.LocalDate;

public interface DateProvider {

    LocalDate getNow();
}
