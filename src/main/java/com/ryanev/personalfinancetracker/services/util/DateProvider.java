package com.ryanev.personalfinancetracker.services.util;

import java.time.LocalDate;

public interface DateProvider {

    LocalDate getNow();
}
