package edu.carroll.EatAndShare.backEnd.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the composite primary key for the RecipeIngredient entity.
 *
 * <p>This class combines the IDs of both {@link Recipe} and {@link Ingredient}
 * to uniquely identify each record in the recipe-ingredient join table.</p>
 *
 * <p>Marked with {@link Embeddable} to indicate that it is not a standalone
 * entity but an embeddable key used by {@link RecipeIngredient}.</p>
 *
 * <p>Implements {@link Serializable} to allow to Hibernate to serialize key objects
 * when persisting or caching.</p>
 *
 * <p>The equals() and hashCode() methods are overridden to ensure correct
 * identity comparison when Hibernate looks up records by composite key.</p>
 *
 * @author Andrias
 * @version 1.0
 * @since 2025-10-11
 */
@Embeddable
public class RecipeIngredientId implements Serializable {

    /** ID of the associated recipe. */
    private Integer recId;

    /** ID of the associated ingredient. */
    private Integer ingredientId;

    /** Default constructor (required by JPA). */
    public RecipeIngredientId() {}

    /**
     * Parameterized constructor for creating a composite key.
     *
     * @param recId the recipe ID
     * @param ingredientId the ingredient ID
     */
    public RecipeIngredientId(Integer recId, Integer ingredientId) {
        this.recId = recId;
        this.ingredientId = ingredientId;
    }

    /** @return the recipe ID */
    public Integer getRecId() {
        return recId;
    }

    /** @param recId sets the recipe ID */
    public void setRecId(Integer recId) {
        this.recId = recId;
    }

    /** @return the ingredient ID */
    public Integer getIngredientId() {
        return ingredientId;
    }

    /** @param ingredientId sets the ingredient ID */
    public void setIngredientId(Integer ingredientId) {
        this.ingredientId = ingredientId;
    }

    /**
     * Compares two RecipeIngredientId objects for equality based on both IDs.
     *
     * @param o the object to compare with
     * @return true if both recipe and ingredient IDs match
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
     * Generates a hash code based on both recipe and ingredient IDs.
     *
     * @return hash code for composite key
     */
    @Override
    public int hashCode() {
        return Objects.hash(recId, ingredientId);
    }
}
