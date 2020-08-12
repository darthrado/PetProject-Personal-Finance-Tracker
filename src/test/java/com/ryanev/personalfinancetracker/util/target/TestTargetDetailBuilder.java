package com.ryanev.personalfinancetracker.util.target;

import com.ryanev.personalfinancetracker.data.entities.Target;
import com.ryanev.personalfinancetracker.data.entities.TargetDetail;
import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.util.user.TestUserBuilder;

import java.time.LocalDate;

public class TestTargetDetailBuilder {
    private final TargetDetail detailToBuild;

    private TestTargetDetailBuilder(TargetDetail targetDetail){
        detailToBuild = targetDetail;
    }

    public static TestTargetDetailBuilder createValidTargetDetail(){

        TargetDetail newTargetDetail = new TargetDetail();
        newTargetDetail.setId(-642L);
        newTargetDetail.setTarget(TestTargetBuilder.createValidTarget().build());
        newTargetDetail.setAmount(666.66);
        newTargetDetail.setValueDate(LocalDate.of(2020,5,5));

        return new TestTargetDetailBuilder(newTargetDetail);
    }

    public TestTargetDetailBuilder withId (Long id){
        detailToBuild.setId(id);

        return this;
    }

    public TestTargetDetailBuilder withTarget(Target target){
        detailToBuild.setTarget(target);

        return this;
    }

    public TestTargetDetailBuilder withAmount(Double amount){
        detailToBuild.setAmount(amount);

        return this;
    }

    public TestTargetDetailBuilder withValueDate(LocalDate date){
        detailToBuild.setValueDate(date);

        return this;
    }

    public TargetDetail build(){
        return detailToBuild;
    }

}
