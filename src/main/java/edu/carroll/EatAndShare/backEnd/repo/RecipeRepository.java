package edu.carroll.EatAndShare.backEnd.repo;

import edu.carroll.EatAndShare.backEnd.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface RecipeRepository extends JpaRepository<Recipe,Integer> {
    List<Recipe> findAllByOrderByIdDesc();
}
