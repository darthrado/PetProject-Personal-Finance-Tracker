package com.ryanev.personalfinancetracker.util.target;

import com.ryanev.personalfinancetracker.data.entities.Target;
import com.ryanev.personalfinancetracker.data.entities.TargetSavings;

public class TestTargetSavingsBuilder {
    private final TargetSavings targetSavingsToBuild;

    private TestTargetSavingsBuilder(TargetSavings targetSavings){
        targetSavingsToBuild = targetSavings;
    }

    public static TestTargetSavingsBuilder createValidTarget(){

        TargetSavings newTarget = new TargetSavings();
        newTarget.setTargetId(-789L);
        newTarget.setTarget(TestTargetBuilder.createValidTarget().withId(-789L).build());

        return new TestTargetSavingsBuilder(newTarget);
    }

    public TestTargetSavingsBuilder withTarget(Target target){
        targetSavingsToBuild.setTargetId(target.getId());
        targetSavingsToBuild.setTarget(target);

        return this;
    }

    public TestTargetSavingsBuilder withTargetId(Long targetId){
        targetSavingsToBuild.setTargetId(targetId);
        targetSavingsToBuild.setTarget(TestTargetBuilder.createValidTarget().withId(targetId).build());

        return this;
    }

    public TargetSavings build(){
        return targetSavingsToBuild;
    }


}
