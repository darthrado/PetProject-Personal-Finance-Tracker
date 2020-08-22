package com.ryanev.personalfinancetracker.services.users;

import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.services.crud_observer.CrudChangeNotifierImpl;
import org.springframework.stereotype.Service;

@Service
public class UserNotifier extends CrudChangeNotifierImpl<User> {
}
