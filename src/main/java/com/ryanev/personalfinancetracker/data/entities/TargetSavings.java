package com.ryanev.personalfinancetracker.data.entities;

import javax.persistence.*;

@Entity
@Table(name = "targets_savings")
public class TargetSavings {

    @Id
    private Long targetId;

    @OneToOne
    @MapsId
    private Target target;

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }
}
