package edu.carroll.eatAndShare.backEnd.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Filename: RecipeIngredientId.java
 * Author: Andrias
 * Date: October 11, 2025
 *
 * Description:
 * Represents the composite primary key for the RecipeIngredient entity.
 * This key uniquely identifies a recipe-ingredient entry by combining
 * both the recipe ID and the ingredient ID. Marked as @Embeddable
 * because it is used inside RecipeIngredient rather than being a
 * standalone entity.
 */
@Embeddable
public class RecipeIngredientId implements Serializable {

    /** ID of the associated recipe. */
    private Integer recId;

    /** ID of the associated ingredient. */
    private Integer ingredientId;

    /** Default constructor required by JPA. */
    public RecipeIngredientId() {}

    /**
     * Constructs a composite key using the provided recipe and ingredient IDs.
     *
     * @param recId the recipe ID
     * @param ingredientId the ingredient ID
     */
    public RecipeIngredientId(Integer recId, Integer ingredientId) {
        this.recId = recId;
        this.ingredientId = ingredientId;
    }

    /**
     * Returns the recipe ID.
     *
     * @return the recipe ID
     */
    public Integer getRecId() {
        return recId;
    }

    /**
     * Sets the recipe ID.
     *
     * @param recId the recipe ID to assign
     */
    public void setRecId(Integer recId) {
        this.recId = recId;
    }

    /**
     * Returns the ingredient ID.
     *
     * @return the ingredient ID
     */
    public Integer getIngredientId() {
        return ingredientId;
    }

    /**
     * Sets the ingredient ID.
     *
     * @param ingredientId the ingredient ID to assign
     */
    public void setIngredientId(Integer ingredientId) {
        this.ingredientId = ingredientId;
    }

    /**
     * Compares this composite key to another object for equality.
     * Two keys are equal if both the recipe ID and ingredient ID match.
     *
     * @param o the object to compare with
     * @return true if both IDs match; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipeIngredientId)) return false;
        RecipeIngredientId that = (RecipeIngredientId) o;
        return Objects.equals(recId, that.recId) &&
                Objects.equals(ingredientId, that.ingredientId);
    }

    /**
     * Generates a hash code for this composite key based on
     * the recipe ID and ingredient ID.
     *
     * @return the generated hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(recId, ingredientId);
    }
}
