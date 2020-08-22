package com.ryanev.personalfinancetracker.services.categories;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.services.crud_observer.*;
import org.springframework.stereotype.Service;

@Service
public class CategoryChangeNotifier extends CrudChangeNotifierImpl<MovementCategory> {
}
