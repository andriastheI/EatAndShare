package edu.carroll.eatAndShare.backEnd.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recipe category in the EatAndShare application.
 *
 * <p>Each category (e.g., "Dessert", "Breakfast", "Vegan") is stored as a
 * separate record in the database and is linked to multiple recipes
 * through a one-to-many relationship.</p>
 *
 * <p>This class maps to the <strong>category</strong> table and includes
 * an auto-generated primary key and a unique name field.</p>
 *
 * <p>Relationships:</p>
 * <ul>
 *   <li>{@link Recipe} – One-to-Many: each category can contain multiple recipes.</li>
 * </ul>
 *
 * <p>The relationship is bidirectional, with {@link Recipe} referencing
 * {@code Category} using the {@code category} field.</p>
 *
 * @author Andrias
 * @version 1.0
 * @since 2025-10-11
 */
@Entity
@Table(name = "category")
public class Category {

    /** Primary key for the category table, generated automatically by the database. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer id;

    /** The unique name of the category (e.g., "Dessert", "Vegan"). */
    @Column(name = "category_name", nullable = false, unique = true, length = 50)
    private String categoryName;

    /**
     * One-to-many relationship with {@link Recipe}.
     * Each category can have multiple recipes assigned to it.
     * Cascade ensures that category changes propagate to its recipes.
     * Orphan removal deletes recipes that are no longer linked.
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recipe> recipes = new ArrayList<>();

    /** @return the category ID */
    public Integer getId() { return id; }

    /** @param id sets the category ID */
    public void setId(Integer id) { this.id = id; }

    /** @return the category name */
    public String getCategoryName() { return categoryName; }

    /** @param categoryName sets the category name */
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    /** @return the list of recipes under this category */
    public List<Recipe> getRecipes() { return recipes; }

    /** @param recipes sets the list of recipes under this category */
    public void setRecipes(List<Recipe> recipes) { this.recipes = recipes; }
}
