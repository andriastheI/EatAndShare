package edu.carroll.eatAndShare.backEnd.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * Filename: Recipe.java
 * Author: Andrias
 * Date: October 11, 2025
 *
 * Description:
 * This entity represents a recipe created by a user in the EatAndShare
 * application. It stores recipe details including title, preparation time,
 * cooking time, difficulty, instructions, and an associated image.
 *
 * Each recipe belongs to a single user and a single category, and contains
 * a list of recipe-ingredient relationships that define the ingredients,
 * quantities, and measurement units required for the dish.
 */

@Entity
@Table(name = "recipe")
public class Recipe {

    /** Serialization identifier for the Recipe entity. */
    public static final long serialVersionUID = 1L;

    /** Auto-generated primary key for the recipe table. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Integer id;

    /**
     * Many-to-one relationship with User.
     * Each recipe is created by exactly one user.
     * Lazy loading improves performance by loading the user only when needed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * One-to-many relationship with RecipeIngredient.
     * Contains all ingredient mappings (with quantities and units)
     * associated with this recipe.
     * Cascade ensures that changes to the recipe propagate to its ingredients.
     */
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> recipeIngredients;

    /**
     * Many-to-one relationship with Category.
     * Assigns the recipe to a category such as "Dinner" or "Dessert".
     * Uses lazy loading for performance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** Title or name of the recipe. */
    @Column(nullable = false, length = 100)
    private String title;

    /** Preparation time in minutes. */
    @Column(name = "prep_time_mins", length = 1000)
    private Integer prepTimeMins;

    /** Cooking time in minutes. */
    @Column(name = "cooking_time_mins", length = 1000)
    private Integer cookTimeMins;

    /** Difficulty level (e.g., Easy, Medium, Hard). */
    @Column
    private String difficulty;

    /** Step-by-step instructions for preparing the recipe. */
    @Column(length = 500000, nullable = false)
    private String instructions;

    /** URL or file path pointing to the recipe's image. */
    @Column(nullable = true, length = 1000)
    private String imgURL;

    /**
     * Returns the user who created this recipe.
     *
     * @return the associated user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user associated with this recipe.
     *
     * @param user the creator of the recipe
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the category assigned to this recipe.
     *
     * @return the recipe category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Sets the category for this recipe.
     *
     * @param category the category to assign
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * Returns all ingredient mappings for this recipe.
     *
     * @return a list of RecipeIngredient objects
     */
    public List<RecipeIngredient> getRecipeIngredients() {
        return recipeIngredients;
    }

    /**
     * Sets the list of ingredient relationships for this recipe.
     *
     * @param recipeIngredients the ingredient mappings to assign
     */
    public void setRecipeIngredients(List<RecipeIngredient> recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
    }

    /**
     * Returns the recipe title.
     *
     * @return the recipe title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the recipe title.
     *
     * @param title the title to assign
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the preparation time in minutes.
     *
     * @return the prep time
     */
    public Integer getPrepTimeMins() {
        return prepTimeMins;
    }

    /**
     * Sets the preparation time in minutes.
     *
     * @param prepTimeMins the prep time to assign
     */
    public void setPrepTimeMins(Integer prepTimeMins) {
        this.prepTimeMins = prepTimeMins;
    }

    /**
     * Returns the cooking time in minutes.
     *
     * @return the cook time
     */
    public Integer getCookTimeMins() {
        return cookTimeMins;
    }

    /**
     * Sets the cooking time in minutes.
     *
     * @param cookTimeMins the cook time to assign
     */
    public void setCookTimeMins(Integer cookTimeMins) {
        this.cookTimeMins = cookTimeMins;
    }

    /**
     * Returns the difficulty level of the recipe.
     *
     * @return the recipe difficulty
     */
    public String getDifficulty() {
        return difficulty;
    }

    /**
     * Sets the difficulty level for the recipe.
     *
     * @param difficulty the difficulty level to assign
     */
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Returns the instructions for preparing the recipe.
     *
     * @return the instructions text
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Sets the instructions for this recipe.
     *
     * @param instructions the instructions to assign
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    /**
     * Returns the image URL or file path for the recipe.
     *
     * @return the image reference
     */
    public String getImgURL() {
        return imgURL;
    }

    /**
     * Sets the image URL or file path for the recipe.
     *
     * @param imgURL the image reference to assign
     */
    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    /**
     * Returns the recipe ID.
     *
     * @return the unique recipe identifier
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the recipe ID. Normally auto-generated, but may be set manually
     * for testing or data imports.
     *
     * @param id the unique recipe identifier
     */
    @Column(unique = true)
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Placeholder method.
     * Currently returns null and is not used.
     *
     * @param o unused parameter
     * @return null
     */
    public Recipe orElse(Object o) {
        return null;
    }

    /**
     * Compares this Recipe object with another for equality.
     * Two recipes are considered equal if all key fields match,
     * including user, ingredients, category, title, prep time,
     * cook time, difficulty, instructions, and image URL.
     *
     * @param o the object to compare with
     * @return true if both recipe objects are equal; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(user, recipe.user) &&
                Objects.equals(recipeIngredients, recipe.recipeIngredients) &&
                Objects.equals(category, recipe.category) &&
                Objects.equals(title, recipe.title) &&
                Objects.equals(prepTimeMins, recipe.prepTimeMins) &&
                Objects.equals(cookTimeMins, recipe.cookTimeMins) &&
                Objects.equals(difficulty, recipe.difficulty) &&
                Objects.equals(instructions, recipe.instructions) &&
                Objects.equals(imgURL, recipe.imgURL);
    }

    /**
     * Generates a hash code for this Recipe object using all
     * relevant fields such as user, ingredients, category, title,
     * times, difficulty, instructions, and image reference.
     *
     * @return the computed hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                user,
                recipeIngredients,
                category,
                title,
                prepTimeMins,
                cookTimeMins,
                difficulty,
                instructions,
                imgURL
        );
    }

}
