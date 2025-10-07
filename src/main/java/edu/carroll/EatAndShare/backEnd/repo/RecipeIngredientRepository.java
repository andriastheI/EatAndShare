package edu.carroll.EatAndShare.backEnd.repo;


import edu.carroll.EatAndShare.backEnd.model.RecipeIngredient;
import edu.carroll.EatAndShare.backEnd.model.RecipeIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, RecipeIngredientId> {}

