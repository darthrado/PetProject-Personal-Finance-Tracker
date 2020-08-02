package com.ryanev.personalfinancetracker.dto.overview.implementations;

import com.ryanev.personalfinancetracker.dto.overview.ReportLineDTO;

public class ReportLineImpl implements ReportLineDTO {

    final String name;
    final Double amount;

    public ReportLineImpl(String name, Double amount,Boolean makeAmountPositive) {
        this.name = name;
        this.amount = makeAmountPositive?Math.abs(amount):amount;
    }

    @Override
    public String getCategoryName() {
        return name;
    }

    @Override
    public Double getAmount() {
        return amount;
    }
}
