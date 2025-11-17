package edu.carroll.eatAndShare.backEnd.service;

import edu.carroll.eatAndShare.backEnd.model.User;
import edu.carroll.eatAndShare.backEnd.form.UserForm;

/**
 * Filename: UserService.java
 * Author: Andrias and Selin
 * Date: October 11, 2025
 *
 * Description:
 * Service boundary for user-related operations such as authentication,
 * registration, password updates, and user retrieval. Implementations
 * provide validation and persistence logic while keeping controllers
 * decoupled from the data layer.
 */
public interface UserService {

    /**
     * Validates a user's credentials during login.
     *
     * @param userForm the submitted login data (username + password)
     * @return true if credentials match a valid user; false otherwise
     */
    boolean validateUser(UserForm userForm);

    /**
     * Registers a new user in the system after performing the
     * necessary validation checks and securely hashing the password.
     *
     * @param user the User entity containing registration information
     * @throws IllegalArgumentException if validation fails or duplicates exist
     */
    void saveUser(User user);

    /**
     * Finds and retrieves a user by username.
     *
     * @param username the username to look up
     * @return the corresponding User, or null if not found
     */
    User findByUsername(String username);

    /**
     * Updates a user's password after verifying the old password.
     *
     * @param username    the username of the account
     * @param oldPassword the current password for verification
     * @param newPassword the new password to save (plain text input)
     * @return true if the password is updated successfully; false otherwise
     * @throws IllegalArgumentException if the user does not exist
     */
    boolean updatePassword(String username, String oldPassword, String newPassword);

}
