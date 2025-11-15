package edu.carroll.eatAndShare.backEnd.service;

import edu.carroll.eatAndShare.backEnd.model.User;
import edu.carroll.eatAndShare.backEnd.form.UserForm;

/**
 * Service interface defining user-related operations for the EatAndShare application.
 *
 * <p>This interface abstracts user authentication, registration, password updates,
 * and retrieval logic. Implementations (such as {@link UserServiceImpl}) provide the
 * actual business logic for interacting with the database and encoding passwords
 * securely.</p>
 *
 * <p>By using an interface, the web layer remains decoupled from the persistence layer,
 * allowing controllers to work with a contract instead of concrete behavior.</p>
 *
 * @author Andrias
 * @version 1.1
 * @since 2025-10-11
 */
public interface UserService {

    /**
     * Validates a user's credentials during login.
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
     * @throws IllegalArgumentException if validation fails or duplicates exist
     */
    void saveUser(User user);

    /**
     * Finds and retrieves a user by their username.
     *
     * @param username the username to search for
     * @return the corresponding {@link User} object, or {@code null} if not found
     */
    User findByUsername(String username);

    /**
     * Updates a user's password after validating their current password.
     *
     * <p>This method verifies the old password using the password encoder before
     * persisting the new one. Controllers should handle UI messaging
     * (success/error display) based on the boolean return value.</p>
     *
     * @param username      username of the account attempting to update password
     * @param oldPassword   current password provided for verification
     * @param newPassword   new password to store (plain text, will be encoded)
     * @return {@code true} if password updated successfully, {@code false} if old password does not match
     * @throws IllegalArgumentException if user does not exist
     */
    boolean updatePassword(String username, String oldPassword, String newPassword);
}
