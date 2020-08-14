package com.ryanev.personalfinancetracker.web.dto.overview.implementations;

import com.ryanev.personalfinancetracker.services.dto.targets.TargetExpensesAndAmountDTO;
import com.ryanev.personalfinancetracker.web.dto.overview.ReportLineDTO;
import com.ryanev.personalfinancetracker.data.entities.Movement;
import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.web.dto.overview.ReportLineWithTargetDTO;

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

    public List<ReportLineWithTargetDTO> createReportFromListOfMovementsWithTargets(List<Movement> movements,
                                                                                    List<TargetExpensesAndAmountDTO> targets){

        Map<String,Double> categoryMovementSumMap = movements.stream()
                .collect(Collectors.groupingBy(movement -> movement.getCategory().getName(),Collectors.summingDouble(Movement::getAmount)));

        Map<String,Double> categoryTargetMap = targets
                .stream()
                .collect(Collectors.toMap(target ->
                                target.getCategoryName(), target -> target.getAmount()!=null?target.getAmount():0 ));

        return  categoryMovementSumMap
                .entrySet()
                .stream()
                .map(entry -> new ReportLineWithTargetDTO(
                        entry.getKey(),
                        entry.getValue(),
                        categoryTargetMap.getOrDefault(entry.getKey(),0.0)))
                .collect(Collectors.toList());
    }
}
