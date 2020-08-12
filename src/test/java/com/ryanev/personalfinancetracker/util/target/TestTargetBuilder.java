package com.ryanev.personalfinancetracker.util.target;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.data.entities.Target;
import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.util.TestCategoryBuilder;
import com.ryanev.personalfinancetracker.util.user.TestUserBuilder;

public class TestTargetBuilder {
    private final Target targetToBuild;

    private TestTargetBuilder(Target target){
        targetToBuild = target;
    }

    public static TestTargetBuilder createValidTarget(){

        Target newTarget = new Target();
        newTarget.setId(-789L);
        newTarget.setUser(TestUserBuilder.createValidUser().build());

        return new TestTargetBuilder(newTarget);
    }

    public TestTargetBuilder withId (Long id){
        targetToBuild.setId(id);

        return this;
    }

    public TestTargetBuilder withUser(User user){
        targetToBuild.setUser(user);

        return this;
    }

    public Target build(){
        return targetToBuild;
    }


}
