package edu.carroll.EatAndShare.backEnd.repo;

import java.util.List;

import edu.carroll.EatAndShare.backEnd.model.Login;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginRepository extends JpaRepository<Login, Integer> {
    // JPA throws an exception if we attempt to return a single object that doesn't exist, so return a list
    // even though we only expect either an empty list or a single element.
    List<Login> findByUsernameIgnoreCase(String username);
}
