package edu.carroll.eatAndShare.backEnd.model;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * Filename: RecipeIngredient.java
 * Author: Andrias
 * Date: October 11, 2025
 *
 * Description:
 * This entity represents the join table between Recipe and Ingredient.
 * Each instance describes a single ingredient used in a specific recipe,
 * including quantity and measurement unit. It maps to the
 * "recipe_ingredient" table using a composite key defined in
 * RecipeIngredientId.
 */
@Entity
@Table(name = "recipe_ingredient")
public class RecipeIngredient {

    /**
     * Composite primary key combining recipe ID and ingredient ID.
     */
    @EmbeddedId
    private RecipeIngredientId id = new RecipeIngredientId();

    /**
     * Many-to-one relationship with Recipe.
     * Maps the recipe ID inside the composite key.
     */
    @ManyToOne
    @MapsId("recId")
    @JoinColumn(name = "rec_id")
    private Recipe recipe;

    /**
     * Many-to-one relationship with Ingredient.
     * Maps the ingredient ID inside the composite key.
     */
    @ManyToOne
    @MapsId("ingredientId")
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    /** Quantity of the ingredient used (e.g., "2"). */
    private String quantity;

    /** Unit of measurement for the ingredient (e.g., "cups", "grams"). */
    private String unit;

    /**
     * Returns the composite key for this recipe-ingredient entry.
     *
     * @return the composite primary key
     */
    public RecipeIngredientId getId() {
        return id;
    }

    /**
     * Sets the composite primary key for this entry.
     *
     * @param id the key to assign
     */
    public void setId(RecipeIngredientId id) {
        this.id = id;
    }

    /**
     * Returns the associated recipe.
     *
     * @return the Recipe object
     */
    public Recipe getRecipe() {
        return recipe;
    }

    /**
     * Sets the associated recipe.
     *
     * @param recipe the recipe to assign
     */
    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    /**
     * Returns the associated ingredient.
     *
     * @return the Ingredient object
     */
    public Ingredient getIngredient() {
        return ingredient;
    }

    /**
     * Sets the associated ingredient.
     *
     * @param ingredient the ingredient to assign
     */
    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    /**
     * Returns the quantity used for this ingredient.
     *
     * @return the quantity value
     */
    public String getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity used for this ingredient.
     *
     * @param quantity the quantity to assign
     */
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    /**
     * Returns the measurement unit for this ingredient.
     *
     * @return the measurement unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the measurement unit for this ingredient.
     *
     * @param unit the unit to assign
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Compares this RecipeIngredient with another for equality.
     * Equality is based on recipe, ingredient, quantity, and unit fields.
     *
     * @param o the object to compare with
     * @return true if both entries are equal; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RecipeIngredient that = (RecipeIngredient) o;
        return Objects.equals(recipe, that.recipe) &&
                Objects.equals(ingredient, that.ingredient) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(unit, that.unit);
    }

    /**
     * Generates a hash code for this RecipeIngredient entry based on
     * its recipe, ingredient, quantity, and measurement unit.
     *
     * @return the computed hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(recipe, ingredient, quantity, unit);
    }
}
