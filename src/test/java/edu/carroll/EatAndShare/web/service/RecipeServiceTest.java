package edu.carroll.EatAndShare.web.service;

import edu.carroll.EatAndShare.backEnd.model.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    /* ------------------------------------------------------------
              ✅  VALID RECIPE TESTS (3 TESTS)
    ------------------------------------------------------------- */

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

        // Arrange: valid ingredient data
        List<String> ingredientNames = List.of("Salt", "Pepper");
        List<String> quantities = List.of("1", "2");
        List<String> units = List.of("tsp", "tbsp");

        // Act + Assert: verify that no exception occurs
        assertDoesNotThrow(() ->
                recipeService.saveRecipe(
                        "Perfect Steak",
                        10,
                        20,
                        "Easy",
                        "Cook it.",
                        ingredientNames,
                        quantities,
                        units,
                        "Dinner",
                        null,
                        USERNAME
                )
        );
    }



/*
    @Test
    public void saveRecipeValidWithImageUploadTest() {
        MockMultipartFile image = new MockMultipartFile(
                "file", "steak.jpg", "image/jpeg", "fake image bytes".getBytes()
        );

        assertDoesNotThrow(
                () -> recipeService.saveRecipe(
                        "Steak with Butter",
                        15, 25,
                        "Medium",
                        "Cook it better.",
                        List.of("Steak"),
                        List.of("1"),
                        List.of("lb"),
                        "Dinner",
                        image,
                        testUser.getUsername()
                )
        );
    }

    @Test
    public void saveRecipeValidCreatesNewCategoryTest() {
        assertDoesNotThrow(
                () -> recipeService.saveRecipe(
                        "Cookies",
                        10, 12,
                        "Easy",
                        "Bake",
                        List.of("Flour"),
                        List.of("2 cups"),
                        List.of("cups"),
                        "Dessert",
                        null,
                        testUser.getUsername()
                )
        );
    }*/

    /* ------------------------------------------------------------
              ❌ INVALID USER TESTS (2 TESTS)
    ------------------------------------------------------------- */

    @Test
    public void savingRecipeInvalidUserTest() {

        String unknownUser = "ghostUser";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Ghost Recipe",
                        10,
                        20,
                        "Easy",
                        "Mix everything.",
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        unknownUser
                )
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    public void savingRecipeNullUserThrowsTest() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Ghost Recipe",
                        10,
                        20,
                        "Easy",
                        "Mix everything.",
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        null // ❌
                )
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }

/*
    *//* ------------------------------------------------------------
              ❌ INVALID TITLE TESTS (3 TESTS)
    ------------------------------------------------------------- *//*
    @Test
    public void saveRecipeEmptyTitleTest() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "",
                        10,
                        10,
                        "Easy",
                        "Cook.",
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Title cannot be empty"));
    }

    @Test
    public void saveRecipeNullTitleTest() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        null,
                        10,
                        10,
                        "Easy",
                        "Cook.",
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Title cannot be empty"));
    }

    @Test
    public void saveRecipeTitleTooLongTest() {
        String longTitle = "A".repeat(500);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        longTitle,
                        10,
                        10,
                        "Easy",
                        "Cook.",
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Title too long"));
    }


    *//* ------------------------------------------------------------
              ❌ INVALID TIMES TESTS (3 TESTS)
    ------------------------------------------------------------- *//*

    @Test
    public void saveRecipeNegativePrepTimeThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Recipe",
                        -5,
                        10,
                        "Easy",
                        "Cook.",
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Prep time cannot be negative"));
    }

    @Test
    public void saveRecipeNegativeCookTimeThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Recipe",
                        10,
                        -10,
                        "Easy",
                        "Cook.",
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Cook time cannot be negative"));
    }

    @Test
    public void saveRecipeNullTimesThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Recipe",
                        null,
                        null,
                        "Easy",
                        "Cook.",
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Time is required"));
    }


    *//* ------------------------------------------------------------
              ❌ INVALID DIFFICULTY (2 TESTS)
    ------------------------------------------------------------- *//*

    @Test
    public void saveRecipeNullDifficultyThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Recipe",
                        10,
                        10,
                        null,
                        "Cook.",
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Difficulty required"));
    }

    @Test
    public void saveRecipeInvalidDifficultyThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Recipe",
                        10,
                        10,
                        "Super Hard",
                        "Cook.",
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Invalid difficulty level"));
    }


    *//* ------------------------------------------------------------
              ❌ INVALID INSTRUCTIONS (3 TESTS)
    ------------------------------------------------------------- *//*

    @Test
    public void saveRecipeNullInstructionsThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Recipe",
                        10,
                        10,
                        "Easy",
                        null,
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Instructions cannot be empty"));
    }

    @Test
    public void saveRecipeEmptyInstructionsThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Recipe",
                        10,
                        10,
                        "Easy",
                        "",
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Instructions cannot be empty"));
    }

    @Test
    public void saveRecipeInstructionsTooLongThrows() {
        String text = "A".repeat(70000);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Recipe",
                        10,
                        10,
                        "Easy",
                        text,
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Instructions too long"));
    }


    *//* ------------------------------------------------------------
              ❌ INVALID CATEGORY (2 TESTS)
    ------------------------------------------------------------- *//*

    @Test
    public void saveRecipeNullCategoryThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Recipe",
                        10,
                        10,
                        "Easy",
                        "Cook",
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        null,
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Category required"));
    }

    @Test
    public void saveRecipeCategoryBlankThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Recipe",
                        10,
                        10,
                        "Easy",
                        "Cook",
                        List.of("Salt"),
                        List.of("1"),
                        List.of("tsp"),
                        " ",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Category required"));
    }



    *//* ------------------------------------------------------------
              ❌ INGREDIENT LIST VALIDATION (4 TESTS)
    ------------------------------------------------------------- *//*

    @Test
    public void saveRecipeNullIngredientsThrows() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Recipe",
                        10,
                        10,
                        "Easy",
                        "Cook",
                        null,
                        null,
                        null,
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("Ingredients required"));
    }

    @Test
    public void saveRecipeIngredientNamesMismatchTest() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.saveRecipe(
                        "Recipe",
                        10,
                        10,
                        "Easy",
                        "Cook",
                        List.of("Salt", "Pepper"),
                        List.of("1"), // mismatch size
                        List.of("tsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );

        assertTrue(exception.getMessage().contains("ingredient list sizes mismatch"));
    }

    @Test
    public void saveRecipeIngredientNameBlankIgnoredValidTest() {
        assertDoesNotThrow(
                () -> recipeService.saveRecipe(
                        "Recipe",
                        10,
                        10,
                        "Easy",
                        "Cook",
                        List.of("", "Pepper"), // blank ignored
                        List.of("1", "2"),
                        List.of("tsp", "tbsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );
    }

    @Test
    public void saveRecipeIngredientTrimWorksTest() {
        assertDoesNotThrow(
                () -> recipeService.saveRecipe(
                        "Recipe",
                        10,
                        10,
                        "Easy",
                        "Cook",
                        List.of("  Salt  "), // trim
                        List.of("1"),
                        List.of("tsp"),
                        "Dinner",
                        null,
                        testUser.getUsername()
                )
        );
    }*/

}
