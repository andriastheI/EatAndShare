package edu.carroll.EatAndShare.backEnd.repo;

import java.util.List;

import edu.carroll.EatAndShare.backEnd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    // JPA throws an exception if we attempt to return a single object that doesn't exist, so return a list
    // even though we only expect either an empty list or a single element.
    List<User> findByUsernameIgnoreCase(String username);

    User findByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
}
