package com.ryanev.personalfinancetracker.data.entities;


import javax.persistence.*;

@Entity
@Table(name = "targets_expenses")
public class TargetExpense {

    @Id
    private Long targetId;

    @OneToOne
    @MapsId
    private Target target;

    @OneToOne
    private MovementCategory category;

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

    public MovementCategory getCategory() {
        return category;
    }

    public void setCategory(MovementCategory category) {
        this.category = category;
    }
}
