package edu.carroll.eatAndShare.web.service;

import edu.carroll.eatAndShare.backEnd.model.*;
import edu.carroll.eatAndShare.backEnd.repo.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class RecipeServiceUnitTests {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private UserService userService; // Assuming you have this for registering users

    private final String USERNAME = "alice";
    private final String OTHER_USERNAME = "bob";

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
        user1.setUsername("alice");
        user1.setPassword("password123");

        userService.saveUser(user1);


        User user2 = new User();
        user2.setFirstName("Bob");
        user2.setLastName("Smith");
        user2.setEmail("bob@example.com");
        user2.setUsername("bob");
        user2.setPassword("password123");

        userService.saveUser(user2);




        // Save recipes via RecipeService
        recipeService.saveRecipe(
                "Chocolate Cake",
                15,
                30,
                "EASY",
                "Mix and bake",
                List.of("Flour", "Sugar"),
                List.of("2", "1"),
                List.of("cups", "cup"),
                "Dessert",
                dummyImage,
                "alice"
        );

        recipeService.saveRecipe(
                "Caesar Salad",
                10,
                0,
                "EASY",
                "Mix lettuce and dressing",
                List.of("Lettuce", "Croutons"),
                List.of("1", "0.5"),
                List.of("head", "cup"),
                "Salad",
                dummyImage,
                "alice"
        );

        // Store recipe IDs if needed
        recipe1Id = recipeService.latestRecipes().get(1).getId();
        recipe2Id = recipeService.latestRecipes().get(0).getId();
    }


    // ========== latestRecipes() (List) ==========

    @Test
    void latestRecipes_shouldReturnAllInDescendingOrder() {
        List<Recipe> recipes = recipeService.latestRecipes();
        assertEquals(2, recipes.size());
        assertTrue(recipes.get(0).getId() > recipes.get(1).getId());
    }

    @Test
    void latestRecipes_shouldReturnEmptyListWhenNoneExist() {
        recipeService.deleteRecipeByIdAndUser(recipe1Id, userService.findByUsername(USERNAME));
        recipeService.deleteRecipeByIdAndUser(recipe2Id, userService.findByUsername(USERNAME));
        List<Recipe> recipes = recipeService.latestRecipes();
        assertTrue(recipes.isEmpty());
    }

    // ========== latestRecipes(Pageable) ==========

    @Test
    void latestRecipes_withPagination_shouldReturnPage() {
        Page<Recipe> page = recipeService.latestRecipes(PageRequest.of(0, 5));
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void latestRecipes_withHighPage_returnsEmpty() {
        Page<Recipe> page = recipeService.latestRecipes(PageRequest.of(99, 5));
        assertTrue(page.isEmpty());
    }

    @Test
    void latestRecipes_invalidPageRequest_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            recipeService.latestRecipes(PageRequest.of(-1, 5));
        });
    }

    // ========== getRecipe(id) ==========

    @Test
    void getRecipe_shouldReturnRecipe() {
        Recipe recipe = recipeService.getRecipe(recipe1Id);
        assertNotNull(recipe);
        assertEquals("Chocolate Cake", recipe.getTitle());
    }

    @Test
    void getRecipe_shouldThrowException_whenNotFound() {
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(99999));
    }

    @Test
    void getRecipe_shouldThrowException_whenIdNull() {
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(null));
    }

    @Test
    void getRecipe_afterDelete_shouldThrowNotFound() {
        // make one, delete it, then fetch
        recipeService.saveRecipe(
                "Temp-Delete", 1, 1, "EASY", "x",
                List.of("A"), List.of("1"), List.of("unit"),
                "Dessert", dummyImage, USERNAME
        );
        Integer id = recipeService.latestRecipes().get(0).getId();
        recipeService.deleteRecipeByIdAndUser(id, userService.findByUsername(USERNAME));
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(id));
    }

    // ========== findByCategoryName(String) ==========

    @Test
    void findByCategoryName_shouldReturnRecipes() {
        List<Recipe> results = recipeService.findByCategoryName("Dessert");
        assertEquals(1, results.size());
    }

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

    @Test
    void findByCategoryName_noMatch() {
        List<Recipe> results = recipeService.findByCategoryName("UnknownCategory");
        assertTrue(results.isEmpty());
    }

    @Test
    void findByCategoryName_null_shouldReturnEmpty() {
        List<Recipe> results = recipeService.findByCategoryName(null);
        assertTrue(results.isEmpty());
    }



    // ========== searchRecipes(String, Pageable) ==========

    @Test
    void searchRecipes_shouldReturnResults() {
        Page<Recipe> results = recipeService.searchRecipes("cake", PageRequest.of(0, 5));
        assertEquals(1, results.getTotalElements());
    }

    @Test
    void searchRecipes_noMatch_returnsEmptyPage() {
        Page<Recipe> results = recipeService.searchRecipes("🦄", PageRequest.of(0, 5));
        assertTrue(results.isEmpty());
    }

    @Test
    void searchRecipes_nullQuery_returnsLatest() {
        Page<Recipe> results = recipeService.searchRecipes(null, PageRequest.of(0, 5));
        assertEquals(2, results.getTotalElements());
    }

    @Test
    void searchRecipes_blankQuery_returnsLatest() {
        Page<Recipe> results = recipeService.searchRecipes("   ", PageRequest.of(0, 5));
        assertEquals(2, results.getTotalElements());
    }


    @Test
    void searchRecipes_isCaseInsensitive_andTrimsWhitespace() {
        var page = recipeService.searchRecipes("   ChOcOlAtE   ", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Chocolate Cake", page.getContent().get(0).getTitle());
    }

    @Test
    void searchRecipes_emojiAndSymbols_shouldNotBreak() {
        var page = recipeService.searchRecipes("🍰⚡️", PageRequest.of(0, 10));
        assertNotNull(page);
        // no crash is the main thing; empty is fine
    }
    @Test
    void searchRecipes_sqlInjectionLikeQuery_shouldNotFail() {
        var results = recipeService.searchRecipes("' OR 1=1 --", PageRequest.of(0, 5));
        assertNotNull(results);
    }


    // ========== findByUser(User) ==========

    @Test
    void findByUser_shouldReturnUserRecipes() {
        User user = userService.findByUsername(USERNAME);
        List<Recipe> recipes = recipeService.findByUser(user);
        assertEquals(2, recipes.size());
    }

    @Test
    void findByUser_noRecipes_returnsEmpty() {
        User other = userService.findByUsername(OTHER_USERNAME);
        List<Recipe> recipes = recipeService.findByUser(other);
        assertTrue(recipes.isEmpty());
    }

    @Test
    void findByUser_nullUser_returnsEmpty() {
        List<Recipe> recipes = recipeService.findByUser(null);
        assertTrue(recipes.isEmpty());
    }
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

    @Test
    void deleteRecipeByIdAndUser_shouldDeleteIfUserMatches() {
        recipeService.saveRecipe(
                "Temp Recipe",
                5,
                10,
                "EASY",
                "Just test",
                List.of("TestIngredient"),
                List.of("1"),
                List.of("unit"),
                "Misc",
                dummyImage,
                USERNAME
        );
        Integer id = recipeService.latestRecipes().get(0).getId();
        User user = userService.findByUsername(USERNAME);
        recipeService.deleteRecipeByIdAndUser(id, user);
        assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipe(id));
    }

    @Test
    void deleteRecipeByIdAndUser_shouldNotDeleteIfUserDoesNotMatch() {
        recipeService.saveRecipe(
                "Protected Recipe",
                5,
                10,
                "EASY",
                "Can't delete",
                List.of("Ingredient"),
                List.of("1"),
                List.of("unit"),
                "Misc",
                dummyImage,
                USERNAME
        );
        Integer id = recipeService.latestRecipes().get(0).getId();
        User wrongUser = userService.findByUsername(OTHER_USERNAME);
        recipeService.deleteRecipeByIdAndUser(id, wrongUser);
        Recipe recipe = recipeService.getRecipe(id);
        assertNotNull(recipe);
    }

    @Test
    void deleteRecipeByIdAndUser_shouldNotThrowExceptionOnMissingRecipe() {
        recipeService.deleteRecipeByIdAndUser(9999, userService.findByUsername(USERNAME));
    }


    @Test
    void deleteRecipeByIdAndUser_nullUser_shouldNotThrow_andNotDelete() {
        // Create
        recipeService.saveRecipe(
                "NullUserDelete", 1, 1, "EASY", "x",
                List.of("A"), List.of("1"), List.of("unit"),
                "Dessert", dummyImage, USERNAME
        );
        Integer id = recipeService.latestRecipes().get(0).getId();

        assertDoesNotThrow(() -> recipeService.deleteRecipeByIdAndUser(id, null));

        // Should still exist
        assertNotNull(recipeService.getRecipe(id));
    }

    @Test
    void deleteRecipeByIdAndUser_nullId_shouldNotThrow() {
        assertDoesNotThrow(() -> recipeService.deleteRecipeByIdAndUser(null, userService.findByUsername(USERNAME)));
    }

    @Test
    void deleteRecipeTwice_shouldNotThrowException() {
        recipeService.saveRecipe("DoubleDelete", 1, 1, "EASY", "x",
                List.of("A"), List.of("1"), List.of("unit"),
                "Dessert", dummyImage, USERNAME);

        Integer id = recipeService.latestRecipes().get(0).getId();
        User user = userService.findByUsername(USERNAME);
        recipeService.deleteRecipeByIdAndUser(id, user);

        // Second delete attempt should be safe
        assertDoesNotThrow(() -> recipeService.deleteRecipeByIdAndUser(id, user));
    }




}









