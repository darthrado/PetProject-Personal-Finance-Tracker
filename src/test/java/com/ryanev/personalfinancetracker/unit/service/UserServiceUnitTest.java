package com.ryanev.personalfinancetracker.unit.service;

import com.ryanev.personalfinancetracker.data.repo.users.UserRepository;
import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.services.users.UserService;
import com.ryanev.personalfinancetracker.services.users.UserServiceImpl;
import com.ryanev.personalfinancetracker.util.user.TestUserBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @InjectMocks
    UserService userService = new UserServiceImpl();

    @Mock
    UserRepository userRepository;

    @Test
    public void getUserById_CorrectId_ReturnsCorrectUser(){
        //Arrange
        Long userId = -777L;
        User userToBeReturned = TestUserBuilder.createValidUser().withId(userId).build();

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(userToBeReturned));
        //Act

        User result = userService.getUserById(userId);

        //Assert
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    public void getUserById_IncorrectId_NoSuchElementExceptionIsThrown(){
        //Arrange
        Long userId = 385L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //Act+Assert
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> userService.getUserById(userId));
    }
}
