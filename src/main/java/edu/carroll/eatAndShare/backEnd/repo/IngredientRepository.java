package edu.carroll.eatAndShare.backEnd.repo;

import edu.carroll.eatAndShare.backEnd.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Filename: IngredientRepository.java
 * Author: Andrias
 * Date: October 11, 2025
 *
 * Description:
 * Repository interface for managing Ingredient entities. This interface
 * extends JpaRepository to provide built-in CRUD operations such as
 * saving, deleting, and retrieving ingredients from the database.
 * Includes a custom query method to find ingredients by name while
 * ignoring case sensitivity.
 */
@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {

    /**
     * Finds an ingredient by its name, ignoring case sensitivity.
     *
     * @param ingredientName the ingredient name to search
     * @return an Optional containing the ingredient if found, otherwise empty
     */
    Optional<Ingredient> findByIngredientNameIgnoreCase(String ingredientName);
}
