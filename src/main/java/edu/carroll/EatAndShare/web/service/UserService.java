package edu.carroll.EatAndShare.web.service;

import edu.carroll.EatAndShare.backEnd.model.User;
import edu.carroll.EatAndShare.web.form.UserForm;

/**
 * Service interface defining user-related operations for the EatAndShare application.
 *
 * <p>This interface abstracts user authentication, registration, and retrieval logic.
 * Implementations (such as {@link UserServiceImpl}) provide the actual business logic
 * for interacting with the database and encoding passwords securely.</p>
 *
 * <p>By using an interface, the web layer remains decoupled from the persistence layer,
 * allowing you to swap or test implementations easily without modifying controllers.</p>
 *
 * @author Andrias
 * @version 1.0
 * @since 2025-10-11
 */
public interface UserService {

    /**
     * Validates a user's credentials during login.
     *
     * <p>This method compares the provided password (from {@link UserForm})
     * with the hashed password stored in the database. Returns {@code true}
     * if credentials match, otherwise {@code false}.</p>
     *
     * @param userForm contains login credentials (username and password)
     * @return {@code true} if credentials are valid, otherwise {@code false}
     */
    boolean validateUser(UserForm userForm);

    /**
     * Registers a new user in the system.
     *
     * <p>Performs validation checks (non-empty fields, unique username/email)
     * and securely hashes the password before saving the user to the database.</p>
     *
     * @param user the {@link User} object containing registration data
     * @throws IllegalArgumentException if any validation fails or duplicates exist
     */
    void saveUser(User user);

    /**
     * Finds and retrieves a user by their username.
     *
     * @param username the username to search for
     * @return the corresponding {@link User} object, or {@code null} if not found
     */
    User findByUsername(String username);
}
