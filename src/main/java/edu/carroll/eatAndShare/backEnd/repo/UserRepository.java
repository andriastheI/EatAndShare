package edu.carroll.eatAndShare.backEnd.repo;

import java.util.List;
import edu.carroll.eatAndShare.backEnd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Filename: UserRepository.java
 * Author: Andrias
 * Date: October 11, 2025
 *
 * Description:
 * Repository interface for managing User entities. Extends JpaRepository
 * to provide built-in CRUD operations and includes custom query methods
 * for looking up users by username or email, as well as for checking
 * uniqueness constraints during registration.
 */

public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Finds users whose username matches the given value, ignoring case.
     * Returns a list to avoid exceptions for no-result scenarios.
     *
     * @param username the username to search
     * @return a list of users with the given username (case-insensitive)
     */
    List<User> findByUsernameIgnoreCase(String username);

    /**
     * Finds a user with an exact username match (case-sensitive).
     *
     * @param username the exact username to search
     * @return the matching User, or null if not found
     */
    User findByUsername(String username);

    /**
     * Checks if a user exists with the given username (case-insensitive).
     * Used during registration to prevent duplicate usernames.
     *
     * @param username the username to check
     * @return true if the username already exists; false otherwise
     */
    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Checks if a user exists with the given email address (case-insensitive).
     * Used during registration to enforce unique emails.
     *
     * @param email the email to check
     * @return true if the email already exists; false otherwise
     */
    boolean existsByEmailIgnoreCase(String email);
}
