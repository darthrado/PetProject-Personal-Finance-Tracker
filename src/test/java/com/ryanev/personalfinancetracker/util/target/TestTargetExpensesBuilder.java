package com.ryanev.personalfinancetracker.util.target;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.data.entities.Target;
import com.ryanev.personalfinancetracker.data.entities.TargetExpense;
import com.ryanev.personalfinancetracker.util.TestCategoryBuilder;

public class TestTargetExpensesBuilder {
    private final TargetExpense targetExpenseToBuild;

    private TestTargetExpensesBuilder(TargetExpense targetExpense){
        targetExpenseToBuild = targetExpense;
    }

    public static TestTargetExpensesBuilder createValidTarget(){

        TargetExpense newTargetExpense = new TargetExpense();
        newTargetExpense.setTargetId(-1111L);
        newTargetExpense.setTarget(TestTargetBuilder.createValidTarget().withId(-1111L).build());
        newTargetExpense.setCategory(TestCategoryBuilder.createValidCategory().build());

        return new TestTargetExpensesBuilder(newTargetExpense);
    }

    public TestTargetExpensesBuilder withTarget (Target target){
        targetExpenseToBuild.setTarget(target);
        targetExpenseToBuild.setTargetId(target.getId());

        return this;
    }

    public TestTargetExpensesBuilder withTargetId(Long targetId){
        targetExpenseToBuild.setTargetId(targetId);
        targetExpenseToBuild.setTarget(TestTargetBuilder.createValidTarget().withId(targetId).build());

        return this;
    }

    public TestTargetExpensesBuilder withCategory(MovementCategory category){
        targetExpenseToBuild.setCategory(category);

        return this;
    }
    public TestTargetExpensesBuilder withCategoryName(String categoryName){
        targetExpenseToBuild.setCategory(TestCategoryBuilder.createValidCategory().withName(categoryName).build());

        return this;
    }

    public TargetExpense build(){
        return targetExpenseToBuild;
    }

}
