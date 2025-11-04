package edu.carroll.eatAndShare.backEnd.repo;

import edu.carroll.eatAndShare.backEnd.model.RecipeIngredient;
import edu.carroll.eatAndShare.backEnd.model.RecipeIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for performing CRUD operations on {@link RecipeIngredient} entities.
 *
 * <p>This repository manages the join table that links recipes and ingredients
 * together, using the composite primary key defined in {@link RecipeIngredientId}.
 * Each entry in this repository represents a specific ingredient used in a recipe,
 * along with its quantity and unit.</p>
 *
 * <p>By extending {@link JpaRepository}, it inherits built-in methods for:</p>
 * <ul>
 *   <li>{@code save()} – insert or update a recipe-ingredient entry</li>
 *   <li>{@code findById()} – retrieve an entry by its composite key</li>
 *   <li>{@code findAll()} – list all recipe-ingredient entries</li>
 *   <li>{@code deleteById()} – delete a specific recipe-ingredient mapping</li>
 * </ul>
 *
 * <p>No custom queries are required here yet, but you can add them later — for example,
 * to find all ingredients for a specific recipe or all recipes that use a given ingredient.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     RecipeIngredientId id = new RecipeIngredientId(recipeId, ingredientId);
 *     Optional&lt;RecipeIngredient&gt; entry = recipeIngredientRepository.findById(id);
 * </pre>
 *
 * @author Andrias
 * @version 1.0
 * @since 2025-10-11
 */
@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, RecipeIngredientId> {}
