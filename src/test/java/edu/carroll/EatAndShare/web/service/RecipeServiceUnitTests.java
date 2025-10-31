
package edu.carroll.EatAndShare.web.service;

import edu.carroll.EatAndShare.backEnd.model.*;
import edu.carroll.EatAndShare.backEnd.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



/**
 * Integration tests for {@link RecipeService} using an in-memory H2 database.
 * <p>
 * Each test runs inside a transaction that automatically rolls back at the end,
 * ensuring database consistency and isolation across test runs.
 * <p>
 * This class validates correct persistence of recipes, user associations,
 * categories, ingredient relationships, and image file storage under various
 * real-world conditions.
 */
@Transactional
@SpringBootTest
public class RecipeServiceUnitTests {

    /** ---------- Constant test data used across multiple tests ---------- */
    private static final String USERNAME = "testchef";
    private static final String EMAIL = "chef@example.com";
    private static final String CATEGORY = "Dessert";

    /** Service under test */
    @Autowired
    private RecipeService recipeService;

    /** Repository dependencies for validation */
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private RecipeRepository recipeRepo;
    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private IngredientRepository ingredientRepo;
    @Autowired
    private RecipeIngredientRepository recipeIngredientRepo;

    /** Pre-created test user used in all valid recipe creation tests. */
    private User testUser;

    /**
     * Creates and saves a test user in the in-memory database
     * before each test executes. This ensures that valid user data
     * exists for linking recipes.
     */
    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setUsername(USERNAME);
        testUser.setPassword("encodedpass");
        testUser.setEmail(EMAIL);
        testUser.setFirstName("Test");
        testUser.setLastName("Chef");
        userRepo.save(testUser);
    }



    @Test
    public void latestRecipes_list_and_pageable() {
        // save 3 recipes
        recipeService.saveRecipe("R1", 1, 1, "Easy", "x",
                List.of(), List.of(), List.of(), "Dessert", null, USERNAME);
        recipeService.saveRecipe("R2", 1, 1, "Easy", "x",
                List.of(), List.of(), List.of(), "Dessert", null, USERNAME);
        recipeService.saveRecipe("R3", 1, 1, "Easy", "x",
                List.of(), List.of(), List.of(), "Dessert", null, USERNAME);
        // Verify non-paged method returns all recipes and newest first
        List<Recipe> list = recipeService.latestRecipes();
        assertEquals(3, list.size());
        assertEquals("R3", list.get(0).getTitle(), "Newest should be first");

        // Verify pageable version honors page size & ordering (2 recipes per page)
        Page<Recipe> page = recipeService.latestRecipes(PageRequest.of(0, 2, Sort.by("id").descending()));
        assertEquals(2, page.getContent().size(), "Page size honored");
        assertEquals("R3", page.getContent().get(0).getTitle());
    }

    /** Blank query should fall back to latestRecipes(pageable). */
    @Test
    public void searchRecipes_blankQueryFallsBackToLatest() {
        //add one recipe to database
        recipeService.saveRecipe("Alpha", 1, 1, "Easy", "x",
                List.of(), List.of(), List.of(), "Dessert", null, USERNAME);
        //Search with blank input — should fallback to latestRecipes()
        Page<Recipe> blank = recipeService.searchRecipes("  ", PageRequest.of(0, 10, Sort.by("id").descending()));
        assertEquals(1, blank.getTotalElements(), "Blank query should behave like latest");
    }



    /** Unknown username → wrapped RuntimeException with cause message. */
    @Test
    public void saveRecipe_fails_whenUserNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                recipeService.saveRecipe("Ghost", 1, 1, "Easy", "x",
                        List.of(), List.of(), List.of(), "Dessert", null, "no_such_user"));

        assertTrue(ex.getMessage().contains("User not found"), "Propagates 'User not found'");
    }


}

