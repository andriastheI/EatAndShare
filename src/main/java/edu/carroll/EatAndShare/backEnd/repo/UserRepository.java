package edu.carroll.EatAndShare.backEnd.repo;

import java.util.List;
import edu.carroll.EatAndShare.backEnd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for performing CRUD and lookup operations on {@link User} entities.
 *
 * <p>This interface extends {@link JpaRepository}, providing access to
 * standard database operations such as creating, retrieving, updating,
 * and deleting users. It maps directly to the <strong>user</strong> table
 * via the {@link User} entity.</p>
 *
 * <p>Several custom query methods are defined for finding users by
 * username or email and for verifying whether a username or email
 * already exists. These are critical for registration and login flows
 * in the EatAndShare application.</p>
 *
 * <p>Spring Data JPA automatically generates SQL queries for these methods
 * by interpreting their names — no manual query definitions are needed.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     boolean taken = userRepository.existsByUsernameIgnoreCase(\"dre\");
 *     User currentUser = userRepository.findByUsername(\"dre\");
 * </pre>
 *
 * @author Andrias
 * @version 1.0
 * @since 2025-10-11
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Finds users with a username that matches the provided value, ignoring case.
     * <p>Returns a list because JPA throws an exception if a single result is expected
     * but none is found. Typically, this list will contain either one user or be empty.</p>
     *
     * @param username the username to search for
     * @return a list of users matching the given username (case-insensitive)
     */
    List<User> findByUsernameIgnoreCase(String username);

    /**
     * Finds a user with the exact username match (case-sensitive).
     *
     * @param username the username to search for
     * @return the matching {@link User}, or {@code null} if not found
     */
    User findByUsername(String username);

    /**
     * Checks if a user exists with the given username, ignoring case.
     * Useful for validating new registrations to prevent duplicates.
     *
     * @param username the username to check
     * @return {@code true} if a user with this username already exists, otherwise {@code false}
     */
    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Checks if a user exists with the given email, ignoring case.
     * Also used during registration to enforce unique emails.
     *
     * @param email the email address to check
     * @return {@code true} if a user with this email already exists, otherwise {@code false}
     */
    boolean existsByEmailIgnoreCase(String email);
}
