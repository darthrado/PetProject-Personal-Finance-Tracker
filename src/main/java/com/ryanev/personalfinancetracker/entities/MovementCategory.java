package com.ryanev.personalfinancetracker.entities;

import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "movement_categories")
public class MovementCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_mvt_categories")
    @SequenceGenerator(name = "seq_mvt_categories",sequenceName = "seq_mvt_categories", allocationSize = 1)
    private Long id;
    private String name;

    @ManyToOne(targetEntity = User.class,
            cascade = {CascadeType.DETACH,CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH},
            fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "id",
            cascade = {CascadeType.DETACH,CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH},
            fetch = FetchType.LAZY)
    List<Movement> movementsForCategory;




    @Nullable
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
