package com.ryanev.personalfinancetracker.data.repo.movements;

import com.ryanev.personalfinancetracker.data.entities.Movement;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class MovementFilterConditions {
    public static Specification<Movement> withCategoryName(String categoryName){
        if(categoryName==null) {
            return trueValue();
        }

        return (root,query,criteriaBuilder) -> { return criteriaBuilder.equal(root.join("category").get("name"),categoryName); };
    }

    public static Specification<Movement> withUserName(String userName){
        if(userName==null) {
            return trueValue();
        }

        return (root,query,criteriaBuilder) -> { return  criteriaBuilder.equal(root.join("user").get("name"),userName); };
    }

    public static Specification<Movement> withName(String name){
        if(name==null) {
            return trueValue();
        }

        return (root,query,criteriaBuilder) -> { return  criteriaBuilder.equal(root.get("name"),name); };
    }

    public static Specification<Movement> withUserId(Long userId){
        if(userId==null) {
            return trueValue();
        }

        return (root,query,criteriaBuilder) -> { return  criteriaBuilder.equal(root.join("user").get("id"),userId); };
    }

    public static Specification<Movement> withAmountFrom(Double amountFrom){
        if(amountFrom==null) {
            return trueValue();
        }

        return (root,query,criteriaBuilder) -> { return  criteriaBuilder.greaterThanOrEqualTo(root.get("amount"),amountFrom); };
    }
    public static Specification<Movement> withAmountTo(Double amountTo){
        if(amountTo==null) {
            return trueValue();
        }

        return (root,query,criteriaBuilder) -> { return  criteriaBuilder.lessThanOrEqualTo(root.get("amount"),amountTo); };
    }
    public static Specification<Movement> withDateFrom(LocalDate dateFrom){
        if(dateFrom==null) {
            return trueValue();
        }

        return (root,query,criteriaBuilder) -> { return  criteriaBuilder.lessThanOrEqualTo(root.get("valueDate"),dateFrom); };
    }
    public static Specification<Movement> withDateTo(LocalDate dateTo){
        if(dateTo==null) {
            return trueValue();
        }

        return (root,query,criteriaBuilder) -> { return  criteriaBuilder.lessThanOrEqualTo(root.get("valueDate"),dateTo); };
    }

    private static Specification<Movement> trueValue(){
        return (root,query,criteriaBuilder) -> { return  criteriaBuilder.conjunction(); };
    }


}
