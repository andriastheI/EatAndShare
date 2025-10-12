package edu.carroll.EatAndShare.web.service;

import edu.carroll.EatAndShare.backEnd.model.*;
import edu.carroll.EatAndShare.backEnd.repo.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Spring Boot test using application-test.properties,
 * but with all repositories mocked to avoid DB writes.
 */
@SpringBootTest
@ActiveProfiles("test")
class RecipeServiceSpringMockTest {

    @Autowired private RecipeService recipeService;

    @MockBean private RecipeRepository recipeRepo;
    @MockBean private IngredientRepository ingredientRepo;
    @MockBean private RecipeIngredientRepository recipeIngredientRepo;
    @MockBean private UserRepository userRepo;
    @MockBean private CategoryRepository categoryRepo;

    private MultipartFile mockImage;

    @BeforeEach
    void setup() throws IOException {
        mockImage = mock(MultipartFile.class);
        when(mockImage.isEmpty()).thenReturn(false);
        when(mockImage.getOriginalFilename()).thenReturn("cake.png");
    }


    @Test
    void saveRecipe_createsEverythingSuccessfully() throws Exception {
        User user = new User(); user.setUsername("selin");
        when(userRepo.findByUsername("selin")).thenReturn(user);

        when(categoryRepo.findByCategoryName("Dessert")).thenReturn(Optional.empty());
        when(categoryRepo.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(1);
            return c;
        });

        when(recipeRepo.save(any(Recipe.class))).thenAnswer(inv -> {
            Recipe r = inv.getArgument(0);
            r.setId(100);
            return r;
        });

        when(ingredientRepo.findByIngredientNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(ingredientRepo.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));

        recipeService.saveRecipe(
                "Cake",
                20, 30,
                "Easy",
                "Mix ingredients and bake.",
                Arrays.asList("Flour", "Sugar"),
                Arrays.asList("2", "1"),
                Arrays.asList("cups", "cup"),
                "Dessert",
                mockImage,
                "selin"
        );

        verify(recipeRepo, times(1)).save(any(Recipe.class));
        verify(categoryRepo, times(1)).save(any(Category.class));
        verify(ingredientRepo, times(2)).save(any(Ingredient.class));
        verify(recipeIngredientRepo, times(2)).save(any(RecipeIngredient.class));
    }


    @Test
    void saveRecipe_throwsWhenUserNotFound() {
        when(userRepo.findByUsername("ghost")).thenReturn(null);

        assertThrows(RuntimeException.class, () ->
                recipeService.saveRecipe(
                        "UnknownUser",
                        10, 15,
                        "Medium",
                        "Test instructions",
                        List.of("Flour"),
                        List.of("1"),
                        List.of("cup"),
                        "Snack",
                        mockImage,
                        "ghost"
                )
        );

        verify(recipeRepo, never()).save(any());
    }

    @Test
    void saveRecipe_handlesIOExceptionGracefully() throws Exception {
        User user = new User(); user.setUsername("selin");
        when(userRepo.findByUsername("selin")).thenReturn(user);

        when(categoryRepo.findByCategoryName(anyString()))
                .thenReturn(Optional.of(new Category()));

        doThrow(new IOException("Disk full")).when(mockImage).transferTo(any(File.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                recipeService.saveRecipe(
                        "BadFile",
                        10, 10,
                        "Easy",
                        "Nope",
                        List.of("Flour"),
                        List.of("1"),
                        List.of("cup"),
                        "Dessert",
                        mockImage,
                        "selin"
                )
        );

        assertTrue(ex.getMessage().contains("Error saving recipe image"));
    }



    @Test
    void saveRecipe_handlesNullImageAndBlankIngredients() {
        User user = new User(); user.setUsername("selin");
        when(userRepo.findByUsername("selin")).thenReturn(user);
        when(categoryRepo.findByCategoryName("Lunch"))
                .thenReturn(Optional.of(new Category()));
        when(recipeRepo.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

        recipeService.saveRecipe(
                "Soup",
                5, 10,
                "Easy",
                "Boil and serve.",
                Arrays.asList("Water", " ", "", "Salt"),
                Arrays.asList("1L", "1", "2", "1"),
                Arrays.asList("litre", "cup", "cup", "tsp"),
                "Lunch",
                null,
                "selin"
        );

        // Skips blank ingredient names
        verify(ingredientRepo, times(2)).save(any(Ingredient.class));
        verify(recipeIngredientRepo, times(2)).save(any(RecipeIngredient.class));
    }

    @Test
    void latestRecipes_returnsRepoResults() {
        Recipe r1 = new Recipe(); r1.setId(1);
        Recipe r2 = new Recipe(); r2.setId(2);
        when(recipeRepo.findAllByOrderByIdDesc()).thenReturn(List.of(r2, r1));

        List<Recipe> result = recipeService.latestRecipes();
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getId());
        verify(recipeRepo, times(1)).findAllByOrderByIdDesc();
    }

    @Test
    void getRecipeOrThrow_behavesCorrectly() {
        Recipe recipe = new Recipe(); recipe.setId(50);
        when(recipeRepo.findById(50)).thenReturn(Optional.of(recipe));
        when(recipeRepo.findById(999)).thenReturn(Optional.empty());

        assertEquals(50, recipeService.getRecipeOrThrow(50).getId());
        assertThrows(RuntimeException.class, () -> recipeService.getRecipeOrThrow(999));
    }
}
