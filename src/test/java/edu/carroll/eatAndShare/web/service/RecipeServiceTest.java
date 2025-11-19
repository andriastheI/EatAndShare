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

}
