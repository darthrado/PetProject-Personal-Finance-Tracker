package com.ryanev.personalfinancetracker.services.targets.expences;

import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryName;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;

public interface TargetCategorySyncService {

    void registerTargetForUserIdAndCategory(Long userId, String categoryName) throws IncorrectUserIdException, IncorrectCategoryName;
    void deleteTargetForUserIdAndCategoryName(Long userId,String categoryName);
    void syncExpenseTargetsWithCategoriesForUser(Long userId) throws IncorrectUserIdException;

}
