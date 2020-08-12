package com.ryanev.personalfinancetracker.data.entities;

import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "target_details")
public class TargetDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_target_details")
    @SequenceGenerator(name = "seq_target_details",sequenceName = "seq_target_details", allocationSize = 1)
    private Long id;

    private LocalDate valueDate;

    @Nullable
    private Double amount;

    @ManyToOne(targetEntity = Target.class,
            cascade = {CascadeType.DETACH,CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH},
            fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private Target target;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    @Nullable
    public Double getAmount() {
        return amount;
    }

    public void setAmount(@Nullable Double amount) {
        this.amount = amount;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }
}
