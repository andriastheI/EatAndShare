package edu.carroll.eatAndShare.web.service;

import edu.carroll.eatAndShare.backEnd.model.Recipe;
import edu.carroll.eatAndShare.backEnd.model.User;
import edu.carroll.eatAndShare.backEnd.service.RecipeService;
import edu.carroll.eatAndShare.backEnd.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite for RecipeService.
 *
 * Responsibilities:
 * - Validate recipe creation logic
 * - Confirm error handling for invalid data
 * - Ensure database persistence behaves correctly
 */
@Transactional
@SpringBootTest
public class RecipeServiceTest {

    /** Service under test for recipe operations */
    @Autowired
    private RecipeService recipeService;

    /** Service for creating and resolving test users */
    @Autowired
    private UserService userService;

    /** ---------- Constant test data used throughout test cases ---------- */
    private static final String USERNAME = "testuser";
    private static final String EMAIL = "testuser@example.com";

    /** Valid recipe values used as baselines for tests */
    private static final String TITLE = "Test Title";
    private static final int PREPTIME = 10;
    private static final int COOKTIME = 10;
    private static final String DIFFCULTY = "Easy";
    private static final String INSTRUCTIONS = "Cook it";
    private static final List<String> INGREDIENTNAMES = List.of("Salt", "Pepper");
    private static final List<String> QUANTITIES = List.of("1", "2");
    private static final List<String> UNITS = List.of("tsp", "tbsp");
    private static final String CATEGORYNAME = "Dinner";

    /** Path to local test image */
    private static final Path IMGPATH = Path.of("src/test/resources/testingimg/testing1.jpg");

    /** Raw image file bytes loaded once for all tests */
    private static final byte[] IMGBYTES;

    static {
        try {
            // Load the test image into memory once
            IMGBYTES = Files.readAllBytes(IMGPATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Mock MultipartFile used to simulate image uploads */
    private static final MultipartFile IMAGE = new MockMultipartFile(
            "image",              // HTML form field name
            "testing1.jpg",      // original file name
            "image/jpeg",        // content type
            IMGBYTES             // file data
    );

    /** User created before each test to isolate DB behavior */
    private User testUser;

    /**
     * Runs before each test to guarantee a clean, valid user exists.
     */
    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setUsername(USERNAME);
        testUser.setPassword("password");
        testUser.setEmail(EMAIL);
        testUser.setFirstName("Test");
        testUser.setLastName("Chef");

        // Persist test user in database
        userService.saveUser(testUser);
    }

    /**
     * Verifies that a valid recipe saves without throwing any exceptions.
     */
    @Test
    public void saveRecipeValidUserValidInputsTest() {

        // Ensure no exception is thrown during valid save
        assertDoesNotThrow(() ->
                recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Validate recipe persisted for user
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.size() == 1);
    }

    /**
     * Ensures duplicate recipe titles are blocked for the same user.
     */
    @Test
    public void saveTheSameTwoRecipeValidUserValidInputsTest() {

        // First recipe save should succeed
        assertDoesNotThrow(() ->
                recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Second save attempt should throw due to duplicate title
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Verify correct error message
        assertEquals("You already have a recipe title saved", exception.getMessage());

        // Confirm only one recipe exists
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.size() == 1);
    }

    /**
     * Confirms that two different recipes can be saved successfully.
     */
    @Test
    public void saveTwoDifferentRecipesValidUserValidInputsTest() {

        // Save first recipe
        assertDoesNotThrow(() ->
                recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        final String validTitle = "Title2";

        // Save second recipe with a different title
        assertDoesNotThrow(() -> recipeService.saveRecipe(
                        validTitle,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Validate both recipes were saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.size() == 2);
    }

    /**
     * Validates that invalid categories are rejected.
     */
    @Test
    public void saveRecipeValidUserInvalidCategoryTest() {
        String invalidCategoryName = "Brunch";

        // Attempt save with invalid category should throw
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        invalidCategoryName,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Confirm correct exception message
        assertEquals("Category not found", exception.getMessage());

        // Ensure nothing was saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Tests behavior when trying to save a recipe using a non-existent user.
     */
    @Test
    public void savingRecipeInvalidUserValidInputsTest() throws IOException {

        // Create user that does not exist in DB
        User invalidUser = new User();
        invalidUser.setUsername("unknown");

        // Save attempt should throw
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        invalidUser.getUsername()
                )
        );

        // Validate message
        assertEquals("User not found: " + invalidUser.getUsername(), exception.getMessage());

        // Confirm no recipes saved
        List<Recipe> recipes = recipeService.findByUser(invalidUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Ensures null usernames are properly rejected.
     */
    @Test
    public void savingRecipeNullUserValidInputThrowsTest() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        null
                )
        );

        // Message should indicate missing user
        assertTrue(exception.getMessage().contains("User not found"));
    }

    /**
     * Tests rejection of empty recipe titles.
     */
    @Test
    public void saveRecipeEmptyTitleTest() {

        String invalidTitle = "";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        invalidTitle,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Validate error
        assertTrue(exception.getMessage().contains("Recipe title cannot be empty"));

        // Confirm nothing saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Tests rejection of null recipe titles.
     */
    @Test
    public void saveRecipeNullTitleTest() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        null,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Must indicate empty title
        assertTrue(exception.getMessage().contains("Recipe title cannot be empty"));

        // Confirm nothing saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Validates that excessively long titles are rejected.
     */
    @Test
    public void saveRecipeTitleTooLongTest() {

        String longTitle = "A".repeat(500);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        longTitle,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Confirm error message
        assertTrue(exception.getMessage().contains("Recipe title is too long"));

        // Confirm nothing saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Tests that negative prep time is rejected.
     */
    @Test
    public void saveRecipeNegativePrepTimeThrows() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        -5,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Check correct validation message
        assertTrue(exception.getMessage().contains("Invalid prep or cook time"));

        // Confirm database remains unchanged
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Ensures that a negative cook time is correctly rejected.
     */
    @Test
    public void saveRecipeNegativeCookTimeThrows() {
        // Attempt to save recipe with invalid negative cook time
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        -10, // invalid cook time
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Verify validation message
        assertTrue(exception.getMessage().contains("Invalid prep or cook time"));

        // Confirm no recipe was saved
        assertTrue(recipeService.findByUser(testUser).isEmpty());
    }

    /**
     * Verifies that zero values for prep and cook time are rejected.
     */
    @Test
    public void saveRecipeZeroTimesThrows() {
        // Attempt to save with zero prep and cooking times
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        0,   // invalid prep time
                        0,   // invalid cook time
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Check proper validation message
        assertTrue(exception.getMessage().contains("Invalid prep or cook time"));

        // Confirm nothing persisted
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Ensures that null prep and cook times are rejected.
     */
    @Test
    public void saveRecipeNullTimesThrows() {
        // Attempt to save with null time values
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        null,  // invalid null prep time
                        null,  // invalid null cook time
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Validate correct exception message
        assertTrue(exception.getMessage().contains("Prep or cook time cannot be null"));

        // Confirm no database changes
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());

        // Double-check message content
        assertTrue(exception.getMessage().contains("Prep or cook time cannot be null"));
    }

    /**
     * Validates rejection of null difficulty values.
     */
    @Test
    public void saveRecipeNullDifficultyThrows() {
        // Attempt to save with missing difficulty
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        null,  // invalid difficulty
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Validate correct error message
        assertTrue(exception.getMessage().contains("Recipe difficulty is null"));

        // Confirm nothing was saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Tests that unsupported difficulty values are rejected.
     */
    @Test
    public void saveRecipeInvalidDifficultyThrows() {
        // Attempt to save recipe with invalid difficulty
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        "Super Hard", // invalid value
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Ensure correct validation message
        assertTrue(exception.getMessage().contains("Invalid difficulty"));

        // Confirm database remains unchanged
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Confirms that null instructions are rejected.
     */
    @Test
    public void saveRecipeNullInstructionsThrows() {
        // Attempt save with null instructions
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        null,  // invalid instructions
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Validate error message
        assertTrue(exception.getMessage().contains("Instructions cannot be empty"));

        // Ensure nothing saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Ensures empty instruction strings are rejected.
     */
    @Test
    public void saveRecipeEmptyInstructionsThrows() {
        final String emptyInstructions = "";

        // Attempt to save with blank instructions
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        emptyInstructions, // invalid instructions
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Validate correct message
        assertTrue(exception.getMessage().contains("Instructions cannot be empty"));

        // Confirm no persistence
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Verifies that excessively long instructions are rejected.
     */
    @Test
    public void saveRecipeInstructionsTooLongThrows() {
        // Create unrealistic large instructions string
        String longInstructions = "A".repeat(700000);

        // Attempt to save
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        longInstructions,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Verify validation message
        assertTrue(exception.getMessage().contains("Instructions too long"));

        // Confirm nothing persisted
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Ensures that null categories are rejected.
     */
    @Test
    public void saveRecipeNullCategoryThrows() {
        // Attempt save with null category
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        null,  // invalid category
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Validate correct error message
        assertTrue(exception.getMessage().contains("Recipe category name is invalid."));

        // Confirm no persistence
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Verifies that empty category values are rejected.
     */
    @Test
    public void saveRecipeEmptyCategoryThrows() {
        final String emptyCategoryName = "";

        // Attempt to save with empty category
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        emptyCategoryName, // invalid category
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Confirm correct validation message
        assertTrue(exception.getMessage().contains("Recipe category name is invalid."));

        // Ensure nothing saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Ensures that missing ingredient lists are rejected.
     */
    @Test
    public void saveRecipeNullIngredientsThrows() {
        // Attempt save with null ingredient lists
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        null,  // ingredient names
                        null,  // quantities
                        null,  // units
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Validate correct error response
        assertTrue(exception.getMessage().contains("Ingredients required"));

        // Confirm database unchanged
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Validates that mismatched ingredient list sizes are rejected.
     */
    @Test
    public void saveRecipeIngredientNamesMismatchTest() {
        // Attempt save with mismatched ingredient lists
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        List.of("Salt", "Pepper"), // more names
                        List.of("1"),              // fewer quantities
                        List.of("tsp"),            // fewer units
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Confirm mismatch is detected
        assertTrue(exception.getMessage().contains("Ingredient list sizes mismatch"));

        // Confirm no persistence occurred
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Ensures blank ingredient names are rejected.
     */
    @Test
    public void saveRecipeIngredientNameBlankIgnoredInvalidTest() {
        // Attempt save with blank ingredient name
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        List.of("", "Pepper"), // invalid blank name
                        List.of("1", "2"),
                        List.of("tsp", "tbsp"),
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Validate correct rejection message
        assertTrue(exception.getMessage().contains("Ingredient name cannot be blank"));

        // Confirm database unchanged
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    /**
     * Confirms that ingredient names are trimmed and accepted when valid.
     */
    @Test
    public void saveRecipeIngredientTrimWorksTest() {
        // Attempt to save ingredient with extra whitespace
        assertDoesNotThrow(
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        List.of("  Salt  "), // leading/trailing spaces
                        List.of("1"),
                        List.of("tsp"),
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Validate recipe was saved correctly
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.size() == 1);
    }


    // ---------- Helper methods for additional tests ----------

    /**
     * Utility method to quickly create a recipe tied to the test user.
     *
     * @param title        the recipe title
     * @param categoryName the category name
     * @return the ID of the newly created recipe
     */
    private Integer createRecipeForCurrentUser(String title, String categoryName) {
        // Persist a new recipe with minimal required fields
        recipeService.saveRecipe(
                title,
                10,
                20,
                "Easy",
                "Test instructions",
                List.of("Salt"),
                List.of("1"),
                List.of("tsp"),
                categoryName,
                null, // no image
                testUser.getUsername()
        );

        // Return ID of the most recently created recipe
        return recipeService.latestRecipes().get(0).getId();
    }

    /**
     * Creates and persists a new User entity for test scenarios.
     * This helper method is used to generate users with no recipes initially.
     *
     * @param username the username to assign
     * @param email    the email to assign
     * @return the newly created and saved User
     */
    private User createAndSaveUser(String username, String email) {
        // Instantiate a new user object
        User user = new User();

        // Populate required fields
        user.setUsername(username);
        user.setPassword("password");
        user.setEmail(email);
        user.setFirstName("Other");
        user.setLastName("User");

        // Persist the user through the service layer
        userService.saveUser(user);

        // Return the created user
        return user;
    }

    // ========== latestRecipes() (List) ==========

    /**
     * Verifies that latestRecipes() returns recipes
     * in descending order based on ID (newest first).
     */
    @Test
    void latestRecipesShouldReturnAllInDescendingOrder() {
        // Arrange: Seed the database with two recipes
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");
        createRecipeForCurrentUser("Caesar Salad", "Salad");

        // Act: Fetch the latest recipes
        List<Recipe> recipes = recipeService.latestRecipes();

        // Assert: Verify the list is populated correctly
        assertNotNull(recipes);
        assertEquals(2, recipes.size(), "There should be exactly 2 recipes");

        // Extract order for verification
        Recipe newest = recipes.get(0);
        Recipe secondNewest = recipes.get(1);

        // Ensure ordering is correct
        assertTrue(newest.getId() > secondNewest.getId(),
                "First recipe should have a higher ID than the second (newest first)");

        // Validate titles and ownership
        assertEquals("Caesar Salad", newest.getTitle());
        assertEquals("Chocolate Cake", secondNewest.getTitle());
        assertEquals(USERNAME, newest.getUser().getUsername());
        assertEquals(USERNAME, secondNewest.getUser().getUsername());
    }

    /**
     * Ensures latestRecipes() returns an empty list when no data exists.
     */
    @Test
    void latestRecipesShouldReturnEmptyListWhenNoneExist() {
        // Act: Retrieve recipes when DB is empty
        List<Recipe> recipes = recipeService.latestRecipes();

        // Assert: Verify safe empty behavior
        assertNotNull(recipes, "Should never return null");
        assertTrue(recipes.isEmpty(), "When there are no recipes, latestRecipes() should return an empty list");
    }

    // ========== latestRecipes(Pageable) ==========

    /**
     * Validates that paginated latest recipes return correct data.
     */
    @Test
    void latestRecipesWithPaginationShouldReturnPage() {
        // Arrange: Create two recipes
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");
        createRecipeForCurrentUser("Caesar Salad", "Salad");

        // Build pagination request
        PageRequest pageRequest = PageRequest.of(0, 5);

        // Act: Fetch paginated results
        Page<Recipe> page = recipeService.latestRecipes(pageRequest);

        // Assert: Validate page behavior
        assertNotNull(page);
        assertFalse(page.isEmpty(), "Page should contain recipes");
        assertEquals(2, page.getTotalElements(), "Should have exactly 2 recipes in total");
        assertEquals(2, page.getContent().size(), "First page should contain exactly 2 recipes");

        // Confirm descending order
        Recipe newest = page.getContent().get(0);
        Recipe secondNewest = page.getContent().get(1);
        assertTrue(newest.getId() > secondNewest.getId(),
                "Page content should be in descending ID order");
    }

    /**
     * Verifies that very high page indexes return empty results.
     */
    @Test
    void latestRecipesWithHighPageReturnsEmpty() {
        // Act: Request a page far beyond real dataset
        Page<Recipe> page = recipeService.latestRecipes(PageRequest.of(999, 5));

        // Assert: Empty but valid response
        assertNotNull(page);
        assertTrue(page.isEmpty(), "Very high page index should return an empty page");
        assertEquals(0, page.getNumberOfElements());
    }

    /**
     * Ensures invalid page requests throw an exception.
     */
    @Test
    void latestRecipesInvalidPageRequestShouldThrow() {
        // Act + Assert: Negative page index should fail
        assertThrows(IllegalArgumentException.class, () ->
                recipeService.latestRecipes(PageRequest.of(-1, 5))
        );
    }

    // ========== getRecipe(id) ==========

    /**
     * Confirms that a valid recipe ID returns a populated Recipe object.
     */
    @Test
    void getRecipeShouldReturnRecipe() {
        // Arrange: Create a recipe
        Integer id = createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        // Act: Retrieve recipe
        Recipe recipe = recipeService.getRecipe(id);

        // Assert: Validate object state
        assertNotNull(recipe);
        assertEquals(id, recipe.getId());
        assertEquals("Chocolate Cake", recipe.getTitle());
        assertEquals("Dessert", recipe.getCategory().getCategoryName());
        assertEquals(USERNAME, recipe.getUser().getUsername());
    }

    /**
     * Ensures non-existent IDs throw an exception.
     */
    @Test
    void getRecipeShouldThrowExceptionWhenNotFound() {
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(999999));
    }

    /**
     * Ensures null IDs are rejected.
     */
    @Test
    void getRecipeShouldThrowExceptionWhenIdNull() {
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(null));
    }

    /**
     * Confirms deleted recipes cannot be retrieved.
     */
    @Test
    void getRecipeAfterDeleteShouldThrowNotFound() {
        // Arrange: create and then delete a recipe
        Integer id = createRecipeForCurrentUser("Temp-Delete", "Dessert");
        User owner = testUser;

        // Act: delete recipe
        boolean deleted = recipeService.deleteRecipeByIdAndUser(id, owner);
        assertTrue(deleted, "deleteRecipeByIdAndUser should return true when it deletes");

        // Assert: Retrieval should now fail
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(id));
    }

    // ========== findByCategoryName(String) ==========

    /**
     * Confirms searching by valid category returns results.
     */
    @Test
    void findByCategoryNameShouldReturnRecipes() {
        // Arrange
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        // Act
        List<Recipe> results = recipeService.findByCategoryName("Dessert");

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size(), "Dessert category should have exactly one recipe");

        // Validate correct recipe content
        assertTrue(
                results.stream().anyMatch(r ->
                        "Chocolate Cake".equals(r.getTitle()) &&
                                "Dessert".equals(r.getCategory().getCategoryName()) &&
                                USERNAME.equals(r.getUser().getUsername())),
                "Results should include our 'Chocolate Cake' dessert for testuser"
        );
    }

    /**
     * Ensures category search is case-insensitive.
     */
    @Test
    void findByCategoryNameCaseInsensitive() {
        // Arrange
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        // Act
        List<Recipe> results = recipeService.findByCategoryName("dessert");

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Dessert", results.get(0).getCategory().getCategoryName());
    }

    /**
     * Verifies behavior when extra internal spaces exist in search term.
     */
    @Test
    void findByCategoryNameInternalExtraSpacesCollapsed() {
        // Arrange
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        // Act
        List<Recipe> noMatch = recipeService.findByCategoryName("De  ssert");
        List<Recipe> match = recipeService.findByCategoryName("   Dessert   ");

        // Assert
        assertTrue(noMatch.isEmpty(), "Internal spacing differences should not match 'Dessert'");
        assertFalse(match.isEmpty(), "Leading/trailing spaces around 'Dessert' should still match");
    }

    /**
     * Ensures unknown categories return empty lists.
     */
    @Test
    void findByCategoryNameNoMatch() {
        List<Recipe> results = recipeService.findByCategoryName("UnknownCategory");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * Ensures null category searches safely return empty lists.
     */
    @Test
    void findByCategoryNameNullShouldReturnEmpty() {
        List<Recipe> results = recipeService.findByCategoryName(null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // ========== searchRecipes(String, Pageable) ==========

    /**
     * Confirms keyword search returns valid matches.
     */
    @Test
    void searchRecipesShouldReturnResults() {
        // Arrange
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        // Act
        Page<Recipe> results = recipeService.searchRecipes("cake", PageRequest.of(0, 5));

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Search for 'cake' should return at least one recipe");
        assertTrue(results.getContent().stream()
                .anyMatch(r -> r.getTitle().toLowerCase().contains("cake")));
    }
    /**
     * Ensures searches that return no matches result in an empty page.
     */
    @Test
    void searchRecipesNoMatchReturnsEmptyPage() {
        // Execute a search using an emoji unlikely to exist in titles
        Page<Recipe> results = recipeService.searchRecipes("🦄", PageRequest.of(0, 5));

        // Validate page object and expected empty results
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Unicorn emoji should not match any recipe titles");
        assertEquals(0, results.getTotalElements());
    }

    /**
     * Ensures that a null search query defaults to returning latest recipes.
     */
    @Test
    void searchRecipesNullQueryReturnsLatest() {
        // Arrange: Ensure there is at least one recipe to compare
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        // Create pagination request
        PageRequest pr = PageRequest.of(0, 5);

        // Act: Retrieve both latest recipes and null-query results
        Page<Recipe> latest = recipeService.latestRecipes(pr);
        Page<Recipe> results = recipeService.searchRecipes(null, pr);

        // Extract recipe IDs for comparison
        List<Integer> latestIds = latest.getContent().stream().map(Recipe::getId).toList();
        List<Integer> resultIds = results.getContent().stream().map(Recipe::getId).toList();

        // Assert matching behavior
        assertEquals(latest.getTotalElements(), results.getTotalElements());
        assertEquals(latestIds, resultIds, "Null query should behave like 'no filter' (latest recipes)");
    }

    /**
     * Verifies blank or whitespace-only search queries behave like "no filter".
     */
    @Test
    void searchRecipesBlankQueryReturnsLatest() {
        // Arrange: Ensure data exists
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        PageRequest pr = PageRequest.of(0, 5);

        // Act: Compare latest and blank-search results
        Page<Recipe> latest = recipeService.latestRecipes(pr);
        Page<Recipe> results = recipeService.searchRecipes("   ", pr);

        // Extract IDs for validation
        List<Integer> latestIds = latest.getContent().stream().map(Recipe::getId).toList();
        List<Integer> resultIds = results.getContent().stream().map(Recipe::getId).toList();

        // Assert identical behavior
        assertEquals(latest.getTotalElements(), results.getTotalElements());
        assertEquals(latestIds, resultIds, "Blank query should behave like 'no filter'");
    }

    /**
     * Validates that search logic is case-insensitive and trims whitespace.
     */
    @Test
    void searchRecipesIsCaseInsensitiveAndTrimsWhitespace() {
        // Arrange
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        // Act: Perform trimmed and mixed-case search
        Page<Recipe> page = recipeService.searchRecipes("   ChOcOlAtE   ",
                PageRequest.of(0, 10));

        // Count matching results
        long matches = page.getContent().stream()
                .filter(r -> "Chocolate Cake".equals(r.getTitle()))
                .count();

        // Assert exact match
        assertEquals(1, matches, "Case-insensitive trimmed search should find exactly one 'Chocolate Cake'");
    }

    /**
     * Ensures that emoji or symbol-only searches do not cause errors.
     */
    @Test
    void searchRecipesEmojiAndSymbolsShouldNotBreak() {
        // Act
        Page<Recipe> page = recipeService.searchRecipes("🍰⚡️", PageRequest.of(0, 10));

        // Assert stable behavior
        assertNotNull(page, "Page result should never be null");
        assertTrue(page.isEmpty(), "Emoji-only query should not match any recipes");
    }

    // ========== findByUser(User) ==========

    /**
     * Confirms a valid user returns their associated recipes.
     */
    @Test
    void findByUserShouldReturnUserRecipes() {
        // Arrange: track existing recipe count
        int before = recipeService.findByUser(testUser).size();

        // Add new recipes
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");
        createRecipeForCurrentUser("Caesar Salad", "Salad");

        // Act: retrieve recipes for user
        List<Recipe> recipes = recipeService.findByUser(testUser);

        // Assert: validate data growth
        assertTrue(recipes.size() >= before + 2,
                "User should have at least two more recipes than before");

        // Confirm expected recipe titles exist
        List<String> titles = recipes.stream().map(Recipe::getTitle).toList();
        assertTrue(titles.contains("Chocolate Cake"));
        assertTrue(titles.contains("Caesar Salad"));
    }

    /**
     * Ensures users with no recipes return empty results.
     */
    @Test
    void findByUserNoRecipesReturnsEmpty() {
        // Arrange: create user with no recipes
        User other = createAndSaveUser("oppsUser", "noRecipes@example.com");

        // Act
        List<Recipe> recipes = recipeService.findByUser(other);

        // Assert
        assertNotNull(recipes);
        assertTrue(recipes.isEmpty(), "User with no recipes should get an empty list");
    }

    /**
     * Ensures null users return empty results safely.
     */
    @Test
    void findByUserNullUserReturnsEmpty() {
        // Act
        List<Recipe> recipes = recipeService.findByUser(null);

        // Assert
        assertNotNull(recipes);
        assertTrue(recipes.isEmpty());
    }

    /**
     * Confirms that unsaved users (without IDs) return empty results.
     */
    @Test
    void findByUserWithUserMissingIdShouldReturnEmpty() {
        // Create user that is NOT persisted
        User dummyUser = new User();
        dummyUser.setUsername("ghostUser");
        dummyUser.setPassword("password");
        dummyUser.setEmail("ghost@example.com");
        dummyUser.setFirstName("Ghost");
        dummyUser.setLastName("User");

        // Act
        List<Recipe> recipes = recipeService.findByUser(dummyUser);

        // Assert
        assertNotNull(recipes);
        assertTrue(recipes.isEmpty(), "User without ID should have no recipes");
    }

    // ========== deleteRecipeByIdAndUser() ==========

    /**
     * Verifies that a matching user can successfully delete a recipe.
     */
    @Test
    void deleteRecipeByIdAndUserShouldDeleteIfUserMatchesAndReturnTrue() {
        // Arrange: create recipe
        Integer id = createRecipeForCurrentUser("Temp Recipe", "Lunch");

        // Act: perform delete
        boolean deleted = recipeService.deleteRecipeByIdAndUser(id, testUser);

        // Assert: validate deletion
        assertTrue(deleted, "Should return true when the recipe is actually deleted");
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(id),
                "Deleted recipe should not be retrievable");
    }

    /**
     * Ensures recipes are not deleted when the user does not own them.
     */
    @Test
    void deleteRecipeByIdAndUserShouldNotDeleteIfUserDoesNotMatch() {
        // Arrange: create recipe
        Integer id = createRecipeForCurrentUser("Protected Recipe", "Lunch");
        User wrongUser = createAndSaveUser("otheruser", "other@example.com");

        // Capture original data
        Recipe original = recipeService.getRecipe(id);

        // Act: attempt delete with wrong user
        boolean deleted = recipeService.deleteRecipeByIdAndUser(id, wrongUser);

        // Assert: verify no deletion
        assertFalse(deleted, "Should return false when user does not own the recipe");

        // Confirm recipe still exists
        Recipe stillThere = recipeService.getRecipe(id);
        assertNotNull(stillThere);
        assertEquals(original.getId(), stillThere.getId());
        assertEquals(USERNAME, stillThere.getUser().getUsername());
    }

    /**
     * Ensures deletion of non-existent recipes fails safely.
     */
    @Test
    void deleteRecipeByIdAndUserShouldNotThrowExceptionOnMissingRecipe() {
        // Attempt delete on non-existent ID
        boolean deleted = recipeService.deleteRecipeByIdAndUser(9999, testUser);

        // Assert safe behavior
        assertFalse(deleted, "Should return false when recipe does not exist");
        assertTrue(recipeService.latestRecipes().isEmpty(),
                "Deleting a non-existent recipe should not change recipe count");
    }

    /**
     * Confirms behavior when delete is attempted with a null user.
     */
    @Test
    void deleteRecipeByIdAndUserNullUserShouldNotThrowAndNotDelete() {
        // Arrange: create recipe
        Integer id = createRecipeForCurrentUser("NullUserDelete", "Dessert");
        Recipe original = recipeService.getRecipe(id);

        // Act: delete with null user
        boolean deleted = recipeService.deleteRecipeByIdAndUser(id, null);

        // Assert: deletion did not occur
        assertFalse(deleted, "Should return false when user is null");

        // Confirm recipe still exists
        Recipe stillThere = recipeService.getRecipe(id);
        assertNotNull(stillThere);
        assertEquals(original.getId(), stillThere.getId());
        assertEquals(original.getUser().getUsername(), stillThere.getUser().getUsername());
    }
}
