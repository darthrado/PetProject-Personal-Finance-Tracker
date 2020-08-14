package com.ryanev.personalfinancetracker.web.dto.targets;

import com.ryanev.personalfinancetracker.services.dto.targets.TargetExpensesAndAmountDTO;

import java.util.List;

public class TargetExpensesDTO {
    List<TargetExpensesAndAmountDTO> targets;

    public List<TargetExpensesAndAmountDTO> getTargets() {
        return targets;
    }

    public void setTargets(List<TargetExpensesAndAmountDTO> targets) {
        this.targets = targets;
    }
}
