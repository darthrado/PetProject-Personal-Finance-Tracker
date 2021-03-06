package com.ryanev.personalfinancetracker.util;

import com.ryanev.personalfinancetracker.data.entities.Movement;
import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.services.dto.movements.MovementDTO;
import com.ryanev.personalfinancetracker.util.user.TestUserBuilder;

import java.time.LocalDate;

public class TestMovementBuilder {
    private final Movement movementToBuild;

    private TestMovementBuilder(Movement movement){
        movementToBuild = movement;
    }

    public static TestMovementBuilder createValidMovement(){
        Movement movementToBeCreated = new Movement();
        movementToBeCreated.setId(222L);
        movementToBeCreated.setAmount(1000.33);
        movementToBeCreated.setUser(TestUserBuilder.createValidUser().build());
        movementToBeCreated.setCategory(TestCategoryBuilder.createValidCategory().build());
        movementToBeCreated.setName("TestMovement123");
        movementToBeCreated.setValueDate(LocalDate.of(2020, 1,1));

        return new TestMovementBuilder(movementToBeCreated);

    }

    public TestMovementBuilder withId(Long id){
        movementToBuild.setId(id);

        return this;
    }

    public TestMovementBuilder withAmount(Double amount){
        movementToBuild.setAmount(amount);

        return this;
    }

    public TestMovementBuilder withUser(User user){
        movementToBuild.setUser(user);

        return this;
    }
    public TestMovementBuilder withCategory(MovementCategory category){
        movementToBuild.setCategory(category);

        return this;
    }

    public TestMovementBuilder withName(String name){
        movementToBuild.setName(name);

        return this;
    }

    public TestMovementBuilder withDate(LocalDate date){
        movementToBuild.setValueDate(date);

        return this;
    }

    public TestMovementBuilder withDescription(String description){
        movementToBuild.setDescription(description);

        return this;
    }

    public Movement build(){
        return movementToBuild;
    }

    public MovementDTO buildDTO(){
        MovementDTO dto = new MovementDTO();
        dto.setId(movementToBuild.getId());
        dto.setAmount(movementToBuild.getAmount());
        dto.setName(movementToBuild.getName());
        dto.setDescription(movementToBuild.getDescription());
        dto.setUserId(movementToBuild.getUser().getId());
        dto.setCategory(movementToBuild.getCategory().getName());
        dto.setValueDate(movementToBuild.getValueDate());

        return dto;
    }


}
