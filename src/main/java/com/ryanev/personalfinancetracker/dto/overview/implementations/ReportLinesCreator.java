package com.ryanev.personalfinancetracker.dto.overview.implementations;

import com.ryanev.personalfinancetracker.dto.overview.ReportLineDTO;
import com.ryanev.personalfinancetracker.entities.Movement;
import com.ryanev.personalfinancetracker.entities.MovementCategory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportLinesCreator {
    public List<ReportLineDTO> createReportFromListOfMovements(List<Movement> movements){
        Map<MovementCategory,Double> resultMap = movements.stream()
                .collect(Collectors.groupingBy(Movement::getCategory,Collectors.summingDouble(Movement::getAmount)));

        return resultMap.keySet()
                .stream()
                .map(f -> new ReportLineImpl(f.getName(),resultMap.get(f),true))
                .collect(Collectors.toList());
    }
}
