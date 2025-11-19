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

@Transactional
@SpringBootTest
public class RecipeServiceTest {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private UserService userService;

    /** ---------- Constant test data used throughout test cases ---------- */
    private static final String USERNAME = "testuser";
    private static final String EMAIL = "testuser@example.com";

    // Valid inputs for the recipe
    private static final String TITLE = "Test Title";
    private static final int PREPTIME = 10;
    private static final int COOKTIME = 10;
    private static final String DIFFCULTY = "Easy";
    private static final String INSTRUCTIONS = "Cook it";
    private static final List<String> INGREDIENTNAMES = List.of("Salt", "Pepper");
    private static final List<String> QUANTITIES = List.of("1", "2");
    private static final List<String> UNITS = List.of("tsp", "tbsp");
    private static final String CATEGORYNAME = "Dinner";

    // Load image file
    private static final Path IMGPATH = Path.of("src/test/resources/testingimg/testing1.jpg");
    private static final byte[] IMGBYTES;

    static {
        try {
            IMGBYTES = Files.readAllBytes(IMGPATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final MultipartFile IMAGE = new MockMultipartFile(
            // form field name
            "image",
            // original filename
            "testing1.jpg",
            // content type
            "image/jpeg",
            // file content
            IMGBYTES
    );

    /** Baseline test user created before each test for validation checks. */
    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setUsername(USERNAME);
        testUser.setPassword("password");
        testUser.setEmail(EMAIL);
        testUser.setFirstName("Test");
        testUser.setLastName("Chef");
        userService.saveUser(testUser);
    }
    /**
     * Test Case: Save a recipe when inputs are valid.
     *
     * Expected Result:
     *  - saveRecipe() should run without throwing any exception
     */
    @Test
    public void saveRecipeValidUserValidInputsTest() {

        // Act + Assert: verify that no exception occurs
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
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.size() == 1);
    }

    @Test
    public void saveTheSameTwoRecipeValidUserValidInputsTest() {

        // Act + Assert: verify that no exception occurs
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

        // Act + Assert: verify that no exception occurs
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
        // Assert correct error message
        assertEquals("You already have a recipe title saved", exception.getMessage());

        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.size() == 1);
    }

    @Test
    public void saveTwoDifferentRecipesValidUserValidInputsTest() {

        // Act + Assert: verify that no exception occurs
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

        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.size() == 2);
    }


    @Test
    public void saveRecipeValidUserInvalidCategoryTest() {
        // INVALID category
        String invalidCategoryName = "Brunch";

        // Expect exception
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

        // Assert correct error message
        assertEquals("Category not found", exception.getMessage());

        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    @Test
    public void savingRecipeInvalidUserValidInputsTest() throws IOException {
        // INVALID user not in database
        User invalidUser = new User();
        invalidUser.setUsername("unknown");

        // Expect exception
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

        // Assert correct error message
        assertEquals("User not found: " + invalidUser.getUsername(), exception.getMessage());

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(invalidUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

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

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    public void saveRecipeEmptyTitleTest() {
        // INVALID empty title
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

        assertTrue(exception.getMessage().contains("Recipe title cannot be empty"));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

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

        assertTrue(exception.getMessage().contains("Recipe title cannot be empty"));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

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

        assertTrue(exception.getMessage().contains("Recipe title is too long"));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }



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

        assertTrue(exception.getMessage().contains("Invalid prep or cook time"));
        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    @Test
    public void saveRecipeNegativeCookTimeThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        -10,
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

        assertTrue(exception.getMessage().contains("Invalid prep or cook time"));

        // Assert: NO recipe saved
        assertTrue(recipeService.findByUser(testUser).isEmpty());
    }

    @Test
    public void saveRecipeZeroTimesThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        0,
                        0,
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
        assertTrue(exception.getMessage().contains("Invalid prep or cook time"));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    @Test
    public void saveRecipeNullTimesThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        null,
                        null,
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
        assertTrue(exception.getMessage().contains("Prep or cook time cannot be null"));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
        assertTrue(exception.getMessage().contains("Prep or cook time cannot be null"));
    }


    @Test
    public void saveRecipeNullDifficultyThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        null,
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Recipe difficulty is null"));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    @Test
    public void saveRecipeInvalidDifficultyThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        "Super Hard",
                        INSTRUCTIONS,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Invalid difficulty"));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }



    @Test
    public void saveRecipeNullInstructionsThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        null,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Instructions cannot be empty"));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    @Test
    public void saveRecipeEmptyInstructionsThrows() {
        final String emptyInstructions = "";
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        emptyInstructions,
                        INGREDIENTNAMES,
                        QUANTITIES,
                        UNITS,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Instructions cannot be empty"));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    @Test
    public void saveRecipeInstructionsTooLongThrows() {
        String longInstructions = "A".repeat(700000);

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

        assertTrue(exception.getMessage().contains("Instructions too long"));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }



    @Test
    public void saveRecipeNullCategoryThrows() {
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
                        null,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Recipe category name is invalid."));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    @Test
    public void saveRecipeEmptyCategoryThrows() {
        final String emptyCategoryName = "";
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
                        emptyCategoryName,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Recipe category name is invalid."));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }



    @Test
    public void saveRecipeNullIngredientsThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        null,
                        null,
                        null,
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Ingredients required"));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }

    @Test
    public void saveRecipeIngredientNamesMismatchTest() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        // mismatch size
                        List.of("Salt", "Pepper"),
                        List.of("1"),
                        List.of("tsp"),
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Ingredient list sizes mismatch"));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }


    @Test
    public void saveRecipeIngredientNameBlankIgnoredInvalidTest() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        List.of("", "Pepper"),
                        List.of("1", "2"),
                        List.of("tsp", "tbsp"),
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Ingredient name cannot be blank"));

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.isEmpty());
    }


    @Test
    public void saveRecipeIngredientTrimWorksTest() {
        assertDoesNotThrow(
                () -> recipeService.saveRecipe(
                        TITLE,
                        PREPTIME,
                        COOKTIME,
                        DIFFCULTY,
                        INSTRUCTIONS,
                        List.of("  Salt  "), // trim
                        List.of("1"),
                        List.of("tsp"),
                        CATEGORYNAME,
                        IMAGE,
                        testUser.getUsername()
                )
        );

        // Assert: NO recipe saved
        List<Recipe> recipes = recipeService.findByUser(testUser);
        assertTrue(recipes != null && recipes.size()==1);
    }


    // ---------- Helper methods for additional tests ----------

    /**
     * Convenience method to create a recipe for the baseline test user.
     * Returns the ID of the newly created recipe.
     */
    private Integer createRecipeForCurrentUser(String title, String categoryName) {
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
                null, // image
                testUser.getUsername()
        );
        // newest recipe should now be at index 0
        return recipeService.latestRecipes().get(0).getId();
    }

    /**
     * Convenience method to create another user with no recipes initially.
     */
    private User createAndSaveUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");
        user.setEmail(email);
        user.setFirstName("Other");
        user.setLastName("User");
        userService.saveUser(user);
        return user;
    }

    // ========== latestRecipes() (List) ==========

    @Test
    void latestRecipesShouldReturnAllInDescendingOrder() {
        // DB starts empty for this test.
        // Arrange: create two known recipes for the test user
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");
        createRecipeForCurrentUser("Caesar Salad", "Salad");

        // Act
        List<Recipe> recipes = recipeService.latestRecipes();

        // Assert: we expect exactly the 2 recipes we just created
        assertNotNull(recipes);
        assertEquals(2, recipes.size(), "There should be exactly 2 recipes");

        Recipe newest = recipes.get(0);
        Recipe secondNewest = recipes.get(1);

        assertTrue(newest.getId() > secondNewest.getId(),
                "First recipe should have a higher ID than the second (newest first)");
        assertEquals("Caesar Salad", newest.getTitle());
        assertEquals("Chocolate Cake", secondNewest.getTitle());
        assertEquals(USERNAME, newest.getUser().getUsername());
        assertEquals(USERNAME, secondNewest.getUser().getUsername());
    }

    @Test
    void latestRecipesShouldReturnEmptyListWhenNoneExist() {
        // Act
        List<Recipe> recipes = recipeService.latestRecipes();

        // Assert
        assertNotNull(recipes, "Should never return null");
        assertTrue(recipes.isEmpty(), "When there are no recipes, latestRecipes() should return an empty list");
    }

    // ========== latestRecipes(Pageable) ==========

    @Test
    void latestRecipesWithPaginationShouldReturnPage() {
        // DB starts empty for this test.
        // Arrange: ensure we have exactly two recipes
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");
        createRecipeForCurrentUser("Caesar Salad", "Salad");

        PageRequest pageRequest = PageRequest.of(0, 5);

        // Act
        Page<Recipe> page = recipeService.latestRecipes(pageRequest);

        // Assert
        assertNotNull(page);
        assertFalse(page.isEmpty(), "Page should contain recipes");
        assertEquals(2, page.getTotalElements(), "Should have exactly 2 recipes in total");
        assertEquals(2, page.getContent().size(), "First page should contain exactly 2 recipes");

        Recipe newest = page.getContent().get(0);
        Recipe secondNewest = page.getContent().get(1);
        assertTrue(newest.getId() > secondNewest.getId(),
                "Page content should be in descending ID order");
    }

    @Test
    void latestRecipesWithHighPageReturnsEmpty() {
        Page<Recipe> page = recipeService.latestRecipes(PageRequest.of(999, 5));

        assertNotNull(page);
        assertTrue(page.isEmpty(), "Very high page index should return an empty page");
        assertEquals(0, page.getNumberOfElements());
    }

    @Test
    void latestRecipesInvalidPageRequestShouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
                recipeService.latestRecipes(PageRequest.of(-1, 5))
        );
    }

    // ========== getRecipe(id) ==========

    @Test
    void getRecipeShouldReturnRecipe() {
        // Arrange
        Integer id = createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        // Act
        Recipe recipe = recipeService.getRecipe(id);

        // Assert
        assertNotNull(recipe);
        assertEquals(id, recipe.getId());
        assertEquals("Chocolate Cake", recipe.getTitle());
        assertEquals("Dessert", recipe.getCategory().getCategoryName());
        assertEquals(USERNAME, recipe.getUser().getUsername());
    }

    @Test
    void getRecipeShouldThrowExceptionWhenNotFound() {
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(999999));
    }

    @Test
    void getRecipeShouldThrowExceptionWhenIdNull() {
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(null));
    }

    @Test
    void getRecipeAfterDeleteShouldThrowNotFound() {
        // DB starts empty.
        // Arrange: create then delete recipe
        Integer id = createRecipeForCurrentUser("Temp-Delete", "Dessert");
        User owner = testUser;

        boolean deleted = recipeService.deleteRecipeByIdAndUser(id, owner);
        assertTrue(deleted, "deleteRecipeByIdAndUser should return true when it deletes");

        // Assert: now it should be considered missing
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(id));
    }


    // ========== findByCategoryName(String) ==========

    @Test
    void findByCategoryNameShouldReturnRecipes() {
        // DB starts empty.
        // Arrange
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        // Act
        List<Recipe> results = recipeService.findByCategoryName("Dessert");

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size(), "Dessert category should have exactly one recipe");
        assertTrue(
                results.stream().anyMatch(r ->
                        "Chocolate Cake".equals(r.getTitle()) &&
                                "Dessert".equals(r.getCategory().getCategoryName()) &&
                                USERNAME.equals(r.getUser().getUsername())),
                "Results should include our 'Chocolate Cake' dessert for testuser"
        );
    }

    @Test
    void findByCategoryNameCaseInsensitive() {
        // DB starts empty.
        // Arrange
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        // Act
        List<Recipe> results = recipeService.findByCategoryName("dessert");

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Dessert", results.get(0).getCategory().getCategoryName());
    }

    @Test
    void findByCategoryNameInternalExtraSpacesCollapsed() {
        // Arrange
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        // Act: internal extra spaces should NOT match
        List<Recipe> noMatch = recipeService.findByCategoryName("De  ssert");
        // But leading/trailing spaces should be okay
        List<Recipe> match = recipeService.findByCategoryName("   Dessert   ");

        // Assert
        assertTrue(noMatch.isEmpty(), "Internal spacing differences should not match 'Dessert'");
        assertFalse(match.isEmpty(), "Leading/trailing spaces around 'Dessert' should still match");
    }

    @Test
    void findByCategoryNameNoMatch() {
        List<Recipe> results = recipeService.findByCategoryName("UnknownCategory");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void findByCategoryNameNullShouldReturnEmpty() {
        List<Recipe> results = recipeService.findByCategoryName(null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }


    // ========== searchRecipes(String, Pageable) ==========

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

    @Test
    void searchRecipesNoMatchReturnsEmptyPage() {
        Page<Recipe> results = recipeService.searchRecipes("🦄", PageRequest.of(0, 5));

        assertNotNull(results);
        assertTrue(results.isEmpty(), "Unicorn emoji should not match any recipe titles");
        assertEquals(0, results.getTotalElements());
    }

    @Test
    void searchRecipesNullQueryReturnsLatest() {
        // Arrange: make sure there is at least one recipe
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        PageRequest pr = PageRequest.of(0, 5);

        // Act
        Page<Recipe> latest = recipeService.latestRecipes(pr);
        Page<Recipe> results = recipeService.searchRecipes(null, pr);

        // Assert: same IDs and counts
        List<Integer> latestIds = latest.getContent().stream().map(Recipe::getId).toList();
        List<Integer> resultIds = results.getContent().stream().map(Recipe::getId).toList();

        assertEquals(latest.getTotalElements(), results.getTotalElements());
        assertEquals(latestIds, resultIds, "Null query should behave like 'no filter' (latest recipes)");
    }

    @Test
    void searchRecipesBlankQueryReturnsLatest() {
        // Arrange
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        PageRequest pr = PageRequest.of(0, 5);

        // Act
        Page<Recipe> latest = recipeService.latestRecipes(pr);
        Page<Recipe> results = recipeService.searchRecipes("   ", pr);

        // Assert
        List<Integer> latestIds = latest.getContent().stream().map(Recipe::getId).toList();
        List<Integer> resultIds = results.getContent().stream().map(Recipe::getId).toList();

        assertEquals(latest.getTotalElements(), results.getTotalElements());
        assertEquals(latestIds, resultIds, "Blank query should behave like 'no filter'");
    }

    @Test
    void searchRecipesIsCaseInsensitiveAndTrimsWhitespace() {
        // DB starts empty.
        // Arrange
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");

        // Act
        Page<Recipe> page = recipeService.searchRecipes("   ChOcOlAtE   ",
                PageRequest.of(0, 10));

        // Assert
        long matches = page.getContent().stream()
                .filter(r -> "Chocolate Cake".equals(r.getTitle()))
                .count();

        assertEquals(1, matches, "Case-insensitive trimmed search should find exactly one 'Chocolate Cake'");
    }

    @Test
    void searchRecipesEmojiAndSymbolsShouldNotBreak() {
        Page<Recipe> page = recipeService.searchRecipes("🍰⚡️", PageRequest.of(0, 10));

        assertNotNull(page, "Page result should never be null");
        assertTrue(page.isEmpty(), "Emoji-only query should not match any recipes");
    }



    // ========== findByUser(User) ==========

    @Test
    void findByUserShouldReturnUserRecipes() {
        // Arrange
        int before = recipeService.findByUser(testUser).size();
        createRecipeForCurrentUser("Chocolate Cake", "Dessert");
        createRecipeForCurrentUser("Caesar Salad", "Salad");

        // Act
        List<Recipe> recipes = recipeService.findByUser(testUser);

        // Assert
        assertTrue(recipes.size() >= before + 2,
                "User should have at least two more recipes than before");
        List<String> titles = recipes.stream().map(Recipe::getTitle).toList();
        assertTrue(titles.contains("Chocolate Cake"));
        assertTrue(titles.contains("Caesar Salad"));
    }

    @Test
    void findByUserNoRecipesReturnsEmpty() {
        // Arrange: new user with no recipes
        User other = createAndSaveUser("noRecipesUser", "noRecipes@example.com");

        // Act
        List<Recipe> recipes = recipeService.findByUser(other);

        // Assert
        assertNotNull(recipes);
        assertTrue(recipes.isEmpty(), "User with no recipes should get an empty list");
    }

    @Test
    void findByUserNullUserReturnsEmpty() {
        List<Recipe> recipes = recipeService.findByUser(null);

        assertNotNull(recipes);
        assertTrue(recipes.isEmpty());
    }

    @Test
    void findByUserWithUserMissingIdShouldReturnEmpty() {
        User dummyUser = new User();
        dummyUser.setUsername("ghostUser");
        dummyUser.setPassword("password");
        dummyUser.setEmail("ghost@example.com");
        dummyUser.setFirstName("Ghost");
        dummyUser.setLastName("User");
        // not saved => no ID

        List<Recipe> recipes = recipeService.findByUser(dummyUser);

        assertNotNull(recipes);
        assertTrue(recipes.isEmpty(), "User without ID should have no recipes");
    }


    // ========== deleteRecipeByIdAndUser() ==========

    @Test
    void deleteRecipeByIdAndUserShouldDeleteIfUserMatchesAndReturnTrue() {
        // DB starts empty.
        // Arrange
        Integer id = createRecipeForCurrentUser("Temp Recipe", "Lunch");

        // Act
        boolean deleted = recipeService.deleteRecipeByIdAndUser(id, testUser);

        // Assert
        assertTrue(deleted, "Should return true when the recipe is actually deleted");
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(id),
                "Deleted recipe should not be retrievable");
    }
    @Test
    void deleteRecipeByIdAndUserShouldNotDeleteIfUserDoesNotMatch() {
        // DB starts empty.
        // Arrange: create recipe for testUser
        Integer id = createRecipeForCurrentUser("Protected Recipe", "Lunch");
        User wrongUser = createAndSaveUser("otheruser", "other@example.com");

        Recipe original = recipeService.getRecipe(id);

        // Act
        boolean deleted = recipeService.deleteRecipeByIdAndUser(id, wrongUser);

        // Assert: no deletion happened
        assertFalse(deleted, "Should return false when user does not own the recipe");
        Recipe stillThere = recipeService.getRecipe(id);
        assertNotNull(stillThere);
        assertEquals(original.getId(), stillThere.getId());
        assertEquals(USERNAME, stillThere.getUser().getUsername());
    }

    @Test
    void deleteRecipeByIdAndUserShouldNotThrowExceptionOnMissingRecipe() {
        // DB starts empty, so ID 9999 definitely does not exist.
        boolean deleted = recipeService.deleteRecipeByIdAndUser(9999, testUser);

        assertFalse(deleted, "Should return false when recipe does not exist");
        assertTrue(recipeService.latestRecipes().isEmpty(),
                "Deleting a non-existent recipe should not change recipe count");
    }

    @Test
    void deleteRecipeByIdAndUserNullUserShouldNotThrowAndNotDelete() {
        // DB starts empty.
        // Arrange
        Integer id = createRecipeForCurrentUser("NullUserDelete", "Dessert");
        Recipe original = recipeService.getRecipe(id);

        // Act
        boolean deleted = recipeService.deleteRecipeByIdAndUser(id, null);

        // Assert: should be false and recipe should still be there
        assertFalse(deleted, "Should return false when user is null");
        Recipe stillThere = recipeService.getRecipe(id);
        assertNotNull(stillThere);
        assertEquals(original.getId(), stillThere.getId());
        assertEquals(original.getUser().getUsername(), stillThere.getUser().getUsername());
    }
}
