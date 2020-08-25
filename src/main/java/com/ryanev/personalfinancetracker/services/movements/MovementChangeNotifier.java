package com.ryanev.personalfinancetracker.services.movements;

import com.ryanev.personalfinancetracker.data.entities.Movement;
import com.ryanev.personalfinancetracker.services.crud_observer.CrudChangeNotifierImpl;
import org.springframework.stereotype.Service;

@Service
public class MovementChangeNotifier extends CrudChangeNotifierImpl<Movement> {
}
