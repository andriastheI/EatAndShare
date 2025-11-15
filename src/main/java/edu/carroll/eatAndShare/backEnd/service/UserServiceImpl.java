package edu.carroll.eatAndShare.backEnd.service;

import java.util.List;

import edu.carroll.eatAndShare.backEnd.model.User;
import edu.carroll.eatAndShare.backEnd.repo.UserRepository;
import edu.carroll.eatAndShare.backEnd.form.UserForm;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filename: UserServiceImpl.java
 * Author: Andrias and Selin
 * Date: October 20, 2025
 *
 * Description:
 * Implementation of the UserService interface. Handles:
 * - User registration
 * - Login validation
 * - Password verification and secure updates
 *
 * This class performs application-level validation, interacts with the
 * UserRepository for persistence, and uses the PasswordEncoder for
 * hashing and verifying passwords securely. Logging is used for tracing
 * control flow without exposing sensitive data.
 */
@Service
public class UserServiceImpl implements UserService {

    /** Logger used for debugging and tracking user operations. */
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /** Repository for CRUD and lookup operations on User entities. */
    private final UserRepository loginRepo;

    /** Responsible for secure password hashing and validation. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor-based dependency injection.
     *
     * @param loginRepo       repository for user persistence
     * @param passwordEncoder encoder used for hashing/verifying passwords
     */
    public UserServiceImpl(UserRepository loginRepo, PasswordEncoder passwordEncoder) {
        this.loginRepo = loginRepo;
        this.passwordEncoder = passwordEncoder;
        log.info("UserServiceImpl initialized");
    }

    /**
     * Validates login credentials by:
     * 1. Searching for the username
     * 2. Comparing the raw password with the stored hashed one
     *
     * @param userForm login form containing username and password
     * @return true if credentials match a unique stored user
     */
    @Override
    public boolean validateUser(UserForm userForm) {
        log.info("validateUser START for username='{}'", userForm.getUsername());

        // Retrieve matching user(s)
        List<User> users = loginRepo.findByUsernameIgnoreCase(userForm.getUsername());

        // Must match exactly one entry
        if (users.size() != 1) {
            log.warn("Login failed — username '{}' not found or not unique", userForm.getUsername());
            return false;
        }

        User user = users.getFirst();
        boolean passwordMatch = passwordEncoder.matches(userForm.getPassword(), user.getPassword());

        if (passwordMatch) {
            log.info("Login successful for username='{}'", userForm.getUsername());
        } else {
            log.warn("Login failed — password mismatch for username='{}'", userForm.getUsername());
        }

        return passwordMatch;
    }

    /**
     * Registers a new user by performing validation checks, ensuring
     * required fields are present, enforcing uniqueness, and hashing
     * the password before saving.
     *
     * @param user user object containing registration details
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public void saveUser(User user) {
        log.info("saveUser START — username='{}', email='{}'", user.getUsername(), user.getEmail());

        // -----------------------------
        // Basic field validation
        // -----------------------------
        if (user.getUsername() == null || user.getUsername().isBlank())
            throw new IllegalArgumentException("Username cannot be empty");

        if (user.getUsername().contains(" "))
            throw new IllegalArgumentException("Username cannot contain a space");

        if (user.getUsername().length() < 6)
            throw new IllegalArgumentException("Username cannot be less than 6 characters");

        if (user.getEmail() == null || user.getEmail().isBlank())
            throw new IllegalArgumentException("Email cannot be empty");

        if (user.getEmail().contains(" "))
            throw new IllegalArgumentException("Email cannot contain a space");

        if (user.getPassword() == null || user.getPassword().isBlank())
            throw new IllegalArgumentException("Password cannot be empty");

        if (user.getPassword().length() < 6)
            throw new IllegalArgumentException("Password cannot be less than 6 characters");

        if (user.getPassword().contains(" "))
            throw new IllegalArgumentException("Password cannot contain a space");

        if (user.getFirstName() == null || user.getFirstName().isBlank())
            throw new IllegalArgumentException("First name cannot be empty");

        if (user.getFirstName().contains(" "))
            throw new IllegalArgumentException("First name cannot contain a space");

        if (user.getLastName() == null || user.getLastName().isBlank())
            throw new IllegalArgumentException("Last name cannot be empty");

        if (user.getLastName().contains(" "))
            throw new IllegalArgumentException("Last name cannot contain a space");

        // -----------------------------
        // Uniqueness checks
        // -----------------------------
        if (loginRepo.existsByUsernameIgnoreCase(user.getUsername()))
            throw new IllegalArgumentException("Username already exists");

        if (loginRepo.existsByEmailIgnoreCase(user.getEmail()))
            throw new IllegalArgumentException("Email already registered");

        // -----------------------------
        // Encrypt password before saving
        // -----------------------------
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            loginRepo.save(user);
            log.info("User saved successfully — username='{}'", user.getUsername());
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while saving user '{}': {}", user.getUsername(), e.getMessage());
            throw new IllegalArgumentException("Username or email already exists!");
        }
    }

    /**
     * Retrieves a user by username.
     *
     * @param username the username to search for
     * @return the User if found, otherwise null
     */
    @Override
    public User findByUsername(String username) {
        log.debug("Searching for user by username='{}'", username);
        return loginRepo.findByUsername(username);
    }

    /**
     * Updates a user's password after validating the old one.
     * Applies the same password rules used during registration.
     *
     * @param username    the username of the account
     * @param oldPassword the current password for verification
     * @param newPassword the new password to store (plain text)
     * @return true if update succeeds; false if old password is incorrect
     * @throws IllegalArgumentException if user does not exist or validation fails
     */
    @Override
    public boolean updatePassword(String username, String oldPassword, String newPassword) {
        log.info("Password update START for username='{}'", username);

        User user = loginRepo.findByUsername(username);
        if (user == null)
            throw new IllegalArgumentException("User not found");

        // Validate old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Password update failed — incorrect old password for '{}'", username);
            return false;
        }

        // Validate new password
        if (newPassword == null || newPassword.isBlank())
            throw new IllegalArgumentException("New password cannot be empty");

        if (newPassword.length() < 6)
            throw new IllegalArgumentException("New password must be at least 6 characters");

        if (newPassword.contains(" "))
            throw new IllegalArgumentException("New password cannot contain spaces");

        // Save new password
        user.setPassword(passwordEncoder.encode(newPassword));
        loginRepo.save(user);

        log.info("Password updated successfully for '{}'", username);
        return true;
    }
}
