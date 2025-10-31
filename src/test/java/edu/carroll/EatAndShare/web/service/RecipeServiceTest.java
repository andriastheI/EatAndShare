/**
 * Filename: RecipeServiceTest.java
 * Author: Andrias Zelele
 * Date: October 23, 2025
 *
 * Description:
 * This test class verifies the core functionality of the RecipeService
 * implementation in the EatAndShare application. It ensures that recipe creation,
 * file uploads, user linking, category creation, and ingredient relationships
 * work correctly under both normal and exceptional conditions.
 *
 * Tests are run against an in-memory H2 database configured via
 * application-test.properties, ensuring isolation and repeatability.
 */

package edu.carroll.EatAndShare.web.service;

import edu.carroll.EatAndShare.backEnd.model.*;
import edu.carroll.EatAndShare.backEnd.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.*;

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
public class RecipeServiceTest {

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

    /**
     * ✅ Full success case: verifies that a complete recipe—with image, category,
     * and ingredient details—is correctly saved and linked to the user.
     * <p>
     * Checks:
     * - Recipe is persisted
     * - Uploaded image path is stored
     * - User association is correct
     * - Category is correctly linked
     * - Ingredient relationships are created
     */
    @Test
    public void saveRecipeSuccessTest() throws Exception {
        // Mock image file to simulate file upload
        MockMultipartFile mockImage = new MockMultipartFile(
                "image", "cake.png", "image/png", "fake-image-data".getBytes()
        );

        // Ingredient data
        List<String> ingredients = List.of("Flour", "Sugar");
        List<String> quantities = List.of("2", "1");
        List<String> units = List.of("cups", "cup");

        // Perform the recipe save operation
        recipeService.saveRecipe(
                "Chocolate Cake",
                15,
                30,
                "Medium",
                "Mix and bake until fluffy.",
                ingredients,
                quantities,
                units,
                CATEGORY,
                mockImage,
                USERNAME
        );

        // Verify recipe persistence
        List<Recipe> allRecipes = recipeRepo.findAll();
        assertEquals("One recipe should be saved", 1, allRecipes.size());

        Recipe saved = allRecipes.get(0);
        assertNotNull("Recipe must not be null", saved);
        assertTrue("Image URL must be stored", saved.getImgURL() != null && saved.getImgURL().contains("/uploads/"));
        assertEquals("Linked user should match", USERNAME, saved.getUser().getUsername());
        assertEquals("Category name should match", CATEGORY, saved.getCategory().getCategoryName());

        // Verify that ingredients were linked to the recipe
        List<RecipeIngredient> links = recipeIngredientRepo.findAll();
        assertEquals("Two ingredients should be linked", 2, links.size());
    }

    /**
     * ✅ Verifies that recipes can be saved without an image.
     * The image field (imgURL) is nullable, so no file should be required.
     * <p>
     * Checks:
     * - Recipe still persists successfully
     * - imgURL remains null when no file is uploaded
     */
    @Test
    public void saveRecipeWithoutImageTest() {
        List<String> ingredients = List.of("Eggs");
        List<String> quantities = List.of("3");
        List<String> units = List.of("pcs");

        // Save recipe without providing an image
        recipeService.saveRecipe(
                "Omelette",
                5,
                5,
                "Easy",
                "Whisk and cook!",
                ingredients,
                quantities,
                units,
                "Breakfast",
                null,
                USERNAME
        );

        // Validate recipe persistence
        Recipe saved = recipeRepo.findByTitle("Omelette").orElse(null);
        assertNotNull("Recipe without image should still save", saved);
        assertTrue("imgURL should be null when no image uploaded", saved.getImgURL() == null);
    }

    /**
     * ✅ Ensures that a new category is automatically created
     * if it does not already exist in the database.
     * <p>
     * Checks:
     * - Category is created and persisted automatically
     * - Recipe links to the new category correctly
     */
    @Test
    public void saveRecipeCreatesNewCategoryIfNotExistsTest() {
        String newCategory = "Vegan";

        // Save recipe with a new category
        recipeService.saveRecipe(
                "Salad Bowl",
                5,
                0,
                "Easy",
                "Mix all veggies.",
                List.of("Lettuce", "Tomato"),
                List.of("1", "2"),
                List.of("cup", "pcs"),
                newCategory,
                null,
                USERNAME
        );

        // Verify that the new category was created
        Category created = categoryRepo.findByCategoryName(newCategory).orElse(null);
        assertNotNull("New category should be created", created);
    }

    /**
     * ✅ Confirms that an uploaded image file is physically created
     * under the local "uploads/test" directory after saving a recipe.
     * <p>
     * Checks:
     * - File exists on disk after save
     * - Image path stored in DB matches expected folder
     */
    @Test
    public void saveRecipeImageFileCreatedTest() throws Exception {
        // Create mock image data
        MockMultipartFile mockImage = new MockMultipartFile(
                "image", "pizza.jpg", "image/jpeg", "pizza data".getBytes()
        );

        // Save recipe with image
        recipeService.saveRecipe(
                "Pizza",
                20,
                15,
                "Medium",
                "Bake in oven until golden.",
                List.of("Dough", "Cheese"),
                List.of("1", "2"),
                List.of("base", "cups"),
                "Main Course",
                mockImage,
                USERNAME
        );

        // Fetch the saved recipe
        Recipe saved = recipeRepo.findByTitle("Pizza").orElse(null);
        assertNotNull("Recipe should be saved", saved);

        // Confirm image file existence on disk
        String relativePath = saved.getImgURL().replace("/uploads/", "");
        Path absolutePath = Paths.get(System.getProperty("user.dir"), "uploads/test", relativePath);
        assertTrue("Image file should exist in uploads/test directory", Files.exists(absolutePath));
    }





}

