package edu.carroll.eatAndShare.backEnd.model;

import jakarta.persistence.*;

/**
 * Represents the join table entity between {@link Recipe} and {@link Ingredient}.
 *
 * <p>This entity links recipes and ingredients together using a composite key,
 * allowing each recipe to specify the ingredients used, their quantity,
 * and the unit of measurement (e.g., "2 cups of flour").</p>
 *
 * <p>In database terms, this maps to the <strong>recipe_ingredient</strong> table,
 * which uses a composite primary key defined in {@link RecipeIngredientId}.</p>
 *
 * <p>Relationships:</p>
 * <ul>
 *   <li>{@link Recipe} – Many-to-One: each entry belongs to a single recipe.</li>
 *   <li>{@link Ingredient} – Many-to-One: each entry refers to a single ingredient.</li>
 * </ul>
 *
 * <p>Each row in this table describes a single ingredient used in a recipe,
 * including its quantity and unit of measurement.</p>
 *
 * @author Andrias
 * @version 1.0
 * @since 2025-10-11
 */
@Entity
@Table(name = "recipe_ingredient")
public class RecipeIngredient {

    /**
     * Composite primary key for this entity, defined in {@link RecipeIngredientId}.
     * Combines both recipe ID and ingredient ID into a single key.
     */
    @EmbeddedId
    private RecipeIngredientId id = new RecipeIngredientId();

    /**
     * Many-to-one relationship to the {@link Recipe} entity.
     * The {@code @MapsId("recId")} annotation tells JPA that the recipe ID
     * field inside {@link RecipeIngredientId} maps to this entity relationship.
     */
    @ManyToOne
    @MapsId("recId")
    @JoinColumn(name = "rec_id")
    private Recipe recipe;

    /**
     * Many-to-one relationship to the {@link Ingredient} entity.
     * The {@code @MapsId("ingredientId")} annotation ensures that the
     * ingredient ID field inside {@link RecipeIngredientId} maps correctly.
     */
    @ManyToOne
    @MapsId("ingredientId")
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    /** The quantity of the ingredient used (e.g., "2"). */
    private String quantity;

    /** The measurement unit for the ingredient (e.g., "cups", "grams"). */
    private String unit;

    /** @return the composite key for this recipe-ingredient entry */
    public RecipeIngredientId getId() {
        return id;
    }

    /** @param id sets the composite key for this recipe-ingredient entry */
    public void setId(RecipeIngredientId id) {
        this.id = id;
    }

    /** @return the associated recipe */
    public Recipe getRecipe() {
        return recipe;
    }

    /** @param recipe sets the associated recipe */
    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    /** @return the associated ingredient */
    public Ingredient getIngredient() {
        return ingredient;
    }

    /** @param ingredient sets the associated ingredient */
    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    /** @return the quantity of the ingredient used */
    public String getQuantity() {
        return quantity;
    }

    /** @param quantity sets the quantity of the ingredient used */
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    /** @return the measurement unit for the ingredient */
    public String getUnit() {
        return unit;
    }

    /** @param unit sets the measurement unit for the ingredient */
    public void setUnit(String unit) {
        this.unit = unit;
    }
}
