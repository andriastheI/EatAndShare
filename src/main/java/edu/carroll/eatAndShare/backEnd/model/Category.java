package edu.carroll.eatAndShare.backEnd.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Filename: Category.java
 * Author: Andrias
 * Date: October 11, 2025
 *
 * Description:
 * This entity represents a recipe category in the EatAndShare application.
 * Each category (e.g., "Dessert", "Breakfast", "Vegan") is stored in the
 * database and can contain multiple recipes through a one-to-many relationship.
 *
 * The class maps to the "category" table and includes an auto-generated
 * primary key along with a unique category name.
 */
@Entity
@Table(name = "category")
public class Category {

    /** Auto-generated primary key for the category table. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer id;

    /** Unique name of the category (e.g., "Dessert", "Vegan"). */
    @Column(name = "category_name", nullable = false, unique = true, length = 50)
    private String categoryName;

    /**
     * One-to-many relationship with Recipe.
     * Each category may contain multiple recipes.
     * Cascade ensures category changes propagate to recipes.
     * Orphan removal deletes recipes no longer associated with this category.
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recipe> recipes = new ArrayList<>();

    /**
     * Returns the category ID.
     *
     * @return the ID of the category
     */
    public Integer getId() { return id; }

    /**
     * Sets the category ID.
     *
     * @param id the ID to assign
     */
    public void setId(Integer id) { this.id = id; }

    /**
     * Returns the name of the category.
     *
     * @return the category name
     */
    public String getCategoryName() { return categoryName; }

    /**
     * Sets the category name.
     *
     * @param categoryName the name to assign
     */
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    /**
     * Returns all recipes under this category.
     *
     * @return a list of recipes
     */
    public List<Recipe> getRecipes() { return recipes; }

    /**
     * Replaces the list of recipes under this category.
     *
     * @param recipes list of recipes to assign
     */
    public void setRecipes(List<Recipe> recipes) { this.recipes = recipes; }

    /**
     * Compares this Category object with another for equality.
     * Two categories are considered equal if they share the same
     * category name and contain the same list of recipes.
     *
     * @param o the object to compare with
     * @return true if the objects are equal; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(categoryName, category.categoryName) &&
                Objects.equals(recipes, category.recipes);
    }

    /**
     * Generates a hash code for this Category object, based on the
     * category name and associated recipes list.
     *
     * @return the generated hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(categoryName, recipes);
    }

}
