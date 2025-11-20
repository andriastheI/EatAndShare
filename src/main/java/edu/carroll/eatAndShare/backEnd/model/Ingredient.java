package edu.carroll.eatAndShare.backEnd.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

/**
 * Filename: Ingredient.java
 * Author: Andrias
 * Date: October 11, 2025
 *
 * Description:
 * This entity represents an ingredient within the EatAndShare application.
 * It stores basic information about each ingredient (e.g., "Flour", "Eggs")
 * and connects to recipes through the RecipeIngredient join table, which
 * specifies the ingredient's quantity and unit for each recipe.
 */
@Entity
@Table(name = "ingredient")
public class Ingredient {

    /** Auto-generated primary key for the ingredient table. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Integer id;

    /** Name of the ingredient (e.g., "Flour", "Salt", "Olive Oil"). */
    @Column(nullable = false, name = "ingredient_name")
    private String ingredientName;

    /**
     * One-to-many relationship with RecipeIngredient.
     * An ingredient may appear in multiple recipes, each with its own
     * quantity and measurement unit defined in the join table.
     */
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> recipeIngredients;

    /**
     * Returns the ingredient ID.
     *
     * @return the ID of the ingredient
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ingredient ID.
     *
     * @param id the ID to assign
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Returns the name of the ingredient.
     *
     * @return the ingredient name
     */
    public String getIngredientName() {
        return ingredientName;
    }

    /**
     * Sets the ingredient name.
     *
     * @param ingredientName the name to assign
     */
    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    /**
     * Compares this Ingredient object with another for equality.
     * Two ingredients are considered equal if they share the same
     * ingredient name and the same list of recipe-ingredient mappings.
     *
     * @param o the object to compare with
     * @return true if both objects are equal; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(ingredientName, that.ingredientName) &&
                Objects.equals(recipeIngredients, that.recipeIngredients);
    }

    /**
     * Generates a hash code for this Ingredient object based on the
     * ingredient name and associated recipe-ingredient list.
     *
     * @return the computed hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(ingredientName, recipeIngredients);
    }

}
