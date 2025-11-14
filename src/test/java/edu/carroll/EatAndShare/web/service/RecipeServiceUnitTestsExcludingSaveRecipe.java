package edu.carroll.eatAndShare.web.service;

import edu.carroll.eatAndShare.backEnd.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class RecipeServiceUnitTestsExcludingSaveRecipe {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private UserService userService; // Assuming you have this for registering users

    private final String USERNAME = "alice1234";
    private final String OTHER_USERNAME = "bob1234";

    private final MultipartFile dummyImage = new MockMultipartFile(
            "image", "test.jpg", "image/jpeg", "test image content".getBytes()
    );

    private Integer recipe1Id;
    private Integer recipe2Id;

    @BeforeAll
    void setup() {
        // Create and register User 1
        User user1 = new User();
        user1.setFirstName("Alice");
        user1.setLastName("Doe");
        user1.setEmail("alice@example.com");
        user1.setUsername("alice1234");
        user1.setPassword("password123");

        userService.saveUser(user1);

        // Create and register User 2 (Bob) who starts with no recipes.
        User user2 = new User();
        user2.setFirstName("Bob");
        user2.setLastName("Smith");
        user2.setEmail("bob@example.com");
        user2.setUsername("bob1234");
        user2.setPassword("password123");

        userService.saveUser(user2);




        // Save recipes via RecipeService
        recipeService.saveRecipe(
                "Chocolate Cake",
                15,
                30,
                "Easy",
                "Mix and bake",
                List.of("Flour", "Sugar"),
                List.of("2", "1"),
                List.of("cups", "cup"),
                "Dessert",
                dummyImage,
                "alice1234"
        );

        recipeService.saveRecipe(
                "Caesar Salad",
                10,
                70,
                "Easy",
                "Mix lettuce and dressing",
                List.of("Lettuce", "Croutons"),
                List.of("1", "0.5"),
                List.of("head", "cup"),
                "Salad",
                dummyImage,
                "alice1234"
        );

        // Store recipe IDs if needed
        // latestRecipes() returns newest first, so index 0 is Caesar Salad, 1 is Chocolate Cake.
        recipe1Id = recipeService.latestRecipes().get(1).getId();
        recipe2Id = recipeService.latestRecipes().get(0).getId();
    }


    // ========== latestRecipes() (List) ==========

    /**
     * Happy-path: latestRecipes() without paging should return all recipes ordered
     * by descending ID (newest first).
     */
    @Test
    void latestRecipes_shouldReturnAllInDescendingOrder() {
        List<Recipe> recipes = recipeService.latestRecipes();
        assertEquals(2, recipes.size());
        assertTrue(recipes.get(0).getId() > recipes.get(1).getId());
    }

    /**
     * If all recipes are deleted, latestRecipes() should return an empty list
     * instead of null or throwing an exception.
     */
    @Test
    void latestRecipes_shouldReturnEmptyListWhenNoneExist() {
        recipeService.deleteRecipeByIdAndUser(recipe1Id, userService.findByUsername(USERNAME));
        recipeService.deleteRecipeByIdAndUser(recipe2Id, userService.findByUsername(USERNAME));
        List<Recipe> recipes = recipeService.latestRecipes();
        assertTrue(recipes.isEmpty());
    }

    // ========== latestRecipes(Pageable) ==========
    /**
     * Happy-path with pagination: requesting the first page with a large enough
     * page size should include both recipes and correct total count.
     */
    @Test
    void latestRecipes_withPagination_shouldReturnPage() {
        Page<Recipe> page = recipeService.latestRecipes(PageRequest.of(0, 5));
        assertEquals(2, page.getTotalElements());
    }

    /**
     * Asking for a page index beyond existing data should return an empty page
     * rather than throw an exception.
     */
    @Test
    void latestRecipes_withHighPage_returnsEmpty() {
        Page<Recipe> page = recipeService.latestRecipes(PageRequest.of(99, 5));
        assertTrue(page.isEmpty());
    }

    /**
     * Invalid pagination input (negative page index) should be validated and
     * reported via IllegalArgumentException.
     */
    @Test
    void latestRecipes_invalidPageRequest_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            recipeService.latestRecipes(PageRequest.of(-1, 5));
        });
    }

    // ========== getRecipe(id) ==========
    /**
     * Happy-path: getRecipe() should return a non-null recipe when the ID exists.
     */
    @Test
    void getRecipe_shouldReturnRecipe() {
        Recipe recipe = recipeService.getRecipe(recipe1Id);
        assertNotNull(recipe);
        assertEquals("Chocolate Cake", recipe.getTitle());
    }

    /**
     * getRecipe() should throw an exception when the recipe ID does not exist.
     */
    @Test
    void getRecipe_shouldThrowException_whenNotFound() {
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(99999));
    }

    /**
     * getRecipe() should defensively reject null IDs instead of failing later
     * with a NullPointerException.
     */
    @Test
    void getRecipe_shouldThrowException_whenIdNull() {
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(null));
    }

    /**
     * After a recipe is deleted successfully, getRecipe() should treat it as
     * missing and throw the same "not found" exception.
     */
    @Test
    void getRecipe_afterDelete_shouldThrowNotFound() {
        // make one, delete it, then fetch
        recipeService.saveRecipe(
                "Temp-Delete", 1, 1, "Easy", "x",
                List.of("A"), List.of("1"), List.of("unit"),
                "Dessert", dummyImage, USERNAME
        );
        Integer id = recipeService.latestRecipes().get(0).getId();
        recipeService.deleteRecipeByIdAndUser(id, userService.findByUsername(USERNAME));
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(id));
    }

    // ========== findByCategoryName(String) ==========

    /**
     * Happy-path: existing category name should return the recipes in that category.
     */
    @Test
    void findByCategoryName_shouldReturnRecipes() {
        List<Recipe> results = recipeService.findByCategoryName("Dessert");
        assertEquals(1, results.size());
    }

    /**
     * Category search should be case-insensitive so users can type
     * "dessert" and still find "Dessert".
     */
    @Test
    void findByCategoryName_caseInsensitive() {
        List<Recipe> results = recipeService.findByCategoryName("dessert");
        assertEquals(1, results.size());
    }
    /** Collapsing spaces works; exact tokens must still match the stored category name. */
    @Test
    void findByCategoryName_internalExtraSpaces_collapsed() {
        // Leading/trailing + multi spaces inside are collapsed to single spaces.
        // For "Dessert" there is no internal space, so "De  ssert" SHOULD NOT match.
        List<Recipe> noMatch = recipeService.findByCategoryName("De  ssert");
        assertTrue(noMatch.isEmpty());

        // But extra outer spaces should be fine (already covered by your test).
        List<Recipe> match = recipeService.findByCategoryName("   Dessert   ");
        assertEquals(1, match.size());
    }

    /**
     * Nonexistent category names should return an empty list instead of null.
     */
    @Test
    void findByCategoryName_noMatch() {
        List<Recipe> results = recipeService.findByCategoryName("UnknownCategory");
        assertTrue(results.isEmpty());
    }

    /**
     * Null input for category name should be handled gracefully by returning
     * an empty list, not throwing an exception.
     */
    @Test
    void findByCategoryName_null_shouldReturnEmpty() {
        List<Recipe> results = recipeService.findByCategoryName(null);
        assertTrue(results.isEmpty());
    }



    // ========== searchRecipes(String, Pageable) ==========

    /**
     * Happy-path text search: searchRecipes() should find a recipe when the
     * query matches part of the title.
     */
    @Test
    void searchRecipes_shouldReturnResults() {
        Page<Recipe> results = recipeService.searchRecipes("cake", PageRequest.of(0, 5));
        assertEquals(1, results.getTotalElements());
    }


    /**
     * Non-matching search terms should return an empty page instead of null.
     */
    @Test
    void searchRecipes_noMatch_returnsEmptyPage() {
        Page<Recipe> results = recipeService.searchRecipes("🦄", PageRequest.of(0, 5));
        assertTrue(results.isEmpty());
    }

    /**
     * A null search query should be treated as "no filter" and return the same
     * results as latestRecipes() for that page.
     */
    @Test
    void searchRecipes_nullQuery_returnsLatest() {
        Page<Recipe> results = recipeService.searchRecipes(null, PageRequest.of(0, 5));
        assertEquals(2, results.getTotalElements());
    }

    /**
     * A blank/whitespace-only search query should also be treated as "no filter"
     * and return latest recipes.
     */
    @Test
    void searchRecipes_blankQuery_returnsLatest() {
        Page<Recipe> results = recipeService.searchRecipes("   ", PageRequest.of(0, 5));
        assertEquals(2, results.getTotalElements());
    }

    /**
     * Search should be case-insensitive and should trim unnecessary whitespace
     * around the query string.
     */
    @Test
    void searchRecipes_isCaseInsensitive_andTrimsWhitespace() {
        var page = recipeService.searchRecipes("   ChOcOlAtE   ", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Chocolate Cake", page.getContent().get(0).getTitle());
    }

    /**
     * Queries containing emojis or other non-alphanumeric symbols should not
     * cause errors; an empty result is acceptable as long as nothing crashes.
     */
    @Test
    void searchRecipes_emojiAndSymbols_shouldNotBreak() {
        var page = recipeService.searchRecipes("🍰⚡️", PageRequest.of(0, 10));
        assertNotNull(page);
        // no crash is the main thing; empty is fine
    }

    /**
     * Queries that look like SQL injection payloads should be handled safely
     * and never break the application or expand results incorrectly.
     */
    @Test
    void searchRecipes_sqlInjectionLikeQuery_shouldNotFail() {
        var results = recipeService.searchRecipes("' OR 1=1 --", PageRequest.of(0, 5));
        assertNotNull(results);
    }


    // ========== findByUser(User) ==========

    /**
     * Happy-path: findByUser() should return all recipes owned by the given user.
     */
    @Test
    void findByUser_shouldReturnUserRecipes() {
        User user = userService.findByUsername(USERNAME);
        List<Recipe> recipes = recipeService.findByUser(user);
        assertEquals(2, recipes.size());
    }

    /**
     * A user with no recipes should get an empty list rather than null.
     */
    @Test
    void findByUser_noRecipes_returnsEmpty() {
        User other = userService.findByUsername(OTHER_USERNAME);
        List<Recipe> recipes = recipeService.findByUser(other);
        assertTrue(recipes.isEmpty());
    }

    /**
     * Passing null into findByUser() should be a no-op that returns an empty
     * list instead of throwing an exception.
     */
    @Test
    void findByUser_nullUser_returnsEmpty() {
        List<Recipe> recipes = recipeService.findByUser(null);
        assertTrue(recipes.isEmpty());
    }

    /**
     * A User instance that has never been persisted (no ID) should be treated as
     * having no recipes; the method should return an empty list.
     */
    @Test
    void findByUser_withUserMissingId_shouldReturnEmpty() {
        User dummyUser = new User();
        dummyUser.setUsername("ghostUser"); // not saved, ID is null
        dummyUser.setFirstName("Alice");
        dummyUser.setLastName("Doe");
        dummyUser.setEmail("alice@example.com");

        dummyUser.setPassword("password123");
        List<Recipe> recipes = recipeService.findByUser(dummyUser);
        assertTrue(recipes.isEmpty(), "Should return empty list for user with no ID");
    }

    // ========== deleteRecipeByIdAndUser() ==========

    /**
     * Happy-path delete: when the recipe exists and the user matches the owner,
     * deleteRecipeByIdAndUser() should remove the recipe from the database.
     */
    @Test
    void deleteRecipeByIdAndUser_shouldDeleteIfUserMatches() {
        recipeService.saveRecipe(
                "Temp Recipe",
                5,
                10,
                "Easy",
                "Just test",
                List.of("TestIngredient"),
                List.of("1"),
                List.of("unit"),
                "Lunch",
                dummyImage,
                USERNAME
        );
        Integer id = recipeService.latestRecipes().get(0).getId();
        User user = userService.findByUsername(USERNAME);
        recipeService.deleteRecipeByIdAndUser(id, user);
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(id));
    }

    /**
     * Authorization check: if the user does not own the recipe, deleteRecipeByIdAndUser()
     * should not delete it.
     */
    @Test
    void deleteRecipeByIdAndUser_shouldNotDeleteIfUserDoesNotMatch() {
        recipeService.saveRecipe(
                "Protected Recipe",
                5,
                10,
                "Easy",
                "Can't delete",
                List.of("Ingredient"),
                List.of("1"),
                List.of("unit"),
                "Lunch",
                dummyImage,
                USERNAME
        );
        Integer id = recipeService.latestRecipes().get(0).getId();
        User wrongUser = userService.findByUsername(OTHER_USERNAME);
        recipeService.deleteRecipeByIdAndUser(id, wrongUser);
        Recipe recipe = recipeService.getRecipe(id);
        assertNotNull(recipe);
    }

    /**
     * Idempotency: attempting to delete a recipe that does not exist should
     * complete silently and not throw an exception.
     */
    @Test
    void deleteRecipeByIdAndUser_shouldNotThrowExceptionOnMissingRecipe() {
        recipeService.deleteRecipeByIdAndUser(9999, userService.findByUsername(USERNAME));
    }

    /**
     * If the user is null, deleteRecipeByIdAndUser() should not delete anything
     * and should not throw an exception.
     */
    @Test
    void deleteRecipeByIdAndUser_nullUser_shouldNotThrow_andNotDelete() {
        // Create
        recipeService.saveRecipe(
                "NullUserDelete", 1, 1, "Easy", "x",
                List.of("A"), List.of("1"), List.of("unit"),
                "Dessert", dummyImage, USERNAME
        );
        Integer id = recipeService.latestRecipes().get(0).getId();

        assertDoesNotThrow(() -> recipeService.deleteRecipeByIdAndUser(id, null));

        // Should still exist
        assertNotNull(recipeService.getRecipe(id));
    }

    /**
     * Passing a null recipe ID should be a no-op that does not throw.
     */
    @Test
    void deleteRecipeByIdAndUser_nullId_shouldNotThrow() {
        assertDoesNotThrow(() -> recipeService.deleteRecipeByIdAndUser(null, userService.findByUsername(USERNAME)));
    }

    /**
     * Idempotency check: deleting the same recipe twice should not throw an
     * exception; the second delete should simply do nothing.
     */
    @Test
    void deleteRecipeTwice_shouldNotThrowException() {
        recipeService.saveRecipe("DoubleDelete", 1, 1, "Easy", "x",
                List.of("A"), List.of("1"), List.of("unit"),
                "Dessert", dummyImage, USERNAME);

        Integer id = recipeService.latestRecipes().get(0).getId();
        User user = userService.findByUsername(USERNAME);
        recipeService.deleteRecipeByIdAndUser(id, user);

        // Second delete attempt should be safe
        assertDoesNotThrow(() -> recipeService.deleteRecipeByIdAndUser(id, user));
    }




}









