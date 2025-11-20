package edu.carroll.eatAndShare.backEnd.repo;

import edu.carroll.eatAndShare.backEnd.model.RecipeIngredient;
import edu.carroll.eatAndShare.backEnd.model.RecipeIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Filename: RecipeIngredientRepository.java
 * Author: Andrias
 * Date: October 11, 2025
 *
 * Description:
 * Repository interface for managing RecipeIngredient entities, which
 * represent the join table linking recipes and ingredients. This
 * interface extends JpaRepository, providing built-in CRUD operations
 * for saving, retrieving, and deleting entries based on the composite
 * primary key defined in RecipeIngredientId.
 */
@Repository
public interface RecipeIngredientRepository
        extends JpaRepository<RecipeIngredient, RecipeIngredientId> {}
