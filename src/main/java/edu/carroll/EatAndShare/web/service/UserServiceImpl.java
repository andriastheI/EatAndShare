package edu.carroll.EatAndShare.web.service;

import java.util.List;

import edu.carroll.EatAndShare.backEnd.model.User;
import edu.carroll.EatAndShare.backEnd.repo.UserRepository;
import edu.carroll.EatAndShare.web.form.UserForm;
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
 * Provides the implementation for managing user-related functionality such as:
 * - Registering new users
 * - Validating login credentials
 * - Password encryption and security checks
 *
 * This class interacts with the {@link UserRepository} to perform database
 * operations such as validating uniqueness constraints and saving new user
 * records. It ensures application-level validation and logs execution flow
 * for debugging purposes.
 */
@Service
public class UserServiceImpl implements UserService {

    /**
     * Logger used for debugging, tracing logic, and capturing important events.
     * Does NOT log sensitive information such as raw passwords.
     */
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /** Repository for accessing and managing user data in the database. */
    private final UserRepository loginRepo;

    /** Used to encode and verify passwords securely. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a UserServiceImpl with required dependencies.
     *
     * @param loginRepo        repository used for performing CRUD operations on users
     * @param passwordEncoder  encoder used for hashing and validating passwords securely
     */
    public UserServiceImpl(UserRepository loginRepo, PasswordEncoder passwordEncoder) {
        this.loginRepo = loginRepo;
        this.passwordEncoder = passwordEncoder;
        log.info("UserServiceImpl initialized");
    }

    /**
     * Validates login credentials by checking:
     * 1. The username exists in the database.
     * 2. The submitted password matches the stored hashed password.
     *
     * @param userForm object containing the username and raw password
     * @return true if valid credentials, false otherwise
     */
    @Override
    public boolean validateUser(UserForm userForm) {
        log.info("validateUser START for username='{}'", userForm.getUsername());

        List<User> users = loginRepo.findByUsernameIgnoreCase(userForm.getUsername());

        if (users.size() != 1) {
            log.warn("Login failed — username '{}' not found or duplicate entries", userForm.getUsername());
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
     * Registers a new user in the database only if:
     * - Required fields are filled (username, password, email, names)
     * - Username and email are unique (case-insensitive)
     *
     * The method encrypts the password before saving to ensure secure storage.
     *
     * @param user user object containing registration details
     * @throws IllegalArgumentException if any validation rule fails
     */
    @Override
    public void saveUser(User user) {
        log.info("saveUser START — username='{}', email='{}'", user.getUsername(), user.getEmail());

        // Validation
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            log.warn("Username validation failed");
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (user.getUsername().contains(" ")) {
            log.warn("Username cannot contain a space");
            throw new IllegalArgumentException("Username cannot contain a space");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Email validation failed");
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (user.getEmail().contains(" ")) {
            log.warn("Email cannot contain a space");
            throw new IllegalArgumentException("Email cannot contain a space");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            log.warn("Password validation failed");
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (user.getPassword().length() < 6) {
            log.warn("Password length validation failed");
            throw new IllegalArgumentException("Password cannot be less than 6 characters");
        }
        if (user.getPassword().contains(" ")) {
            log.warn("Password cannot contain a space");
            throw new IllegalArgumentException("Password cannot contain a space");
        }
        if (user.getUsername().length() < 6) {
            log.warn("Username length validation failed");
            throw new IllegalArgumentException("Username cannot be less than 6 characters");
        }
        if (loginRepo.existsByUsernameIgnoreCase(user.getUsername())) {
            log.warn("Duplicate username detected '{}'", user.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }
        if (loginRepo.existsByEmailIgnoreCase(user.getEmail())) {
            log.warn("Duplicate email detected '{}'", user.getEmail());
            throw new IllegalArgumentException("Email already registered");
        }
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            log.warn("First name validation failed");
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (user.getFirstName().contains(" ")) {
            log.warn("Firstname cannot contain a space");
            throw new IllegalArgumentException("Firstname cannot contain a space");
        }
        if (user.getLastName() == null || user.getLastName().isBlank()) {
            log.warn("Last name validation failed");
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        if (user.getLastName().contains(" ")) {
            log.warn("Lastname cannot contain a space");
            throw new IllegalArgumentException("Lastname cannot contain a space");
        }

        // Hash password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            loginRepo.save(user);
            log.info("User successfully saved — username='{}'", user.getUsername());
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity error while saving user '{}': {}", user.getUsername(), e.getMessage());
            throw new IllegalArgumentException("Username or email already exists!");
        }
    }

    /**
     * Retrieves a user from the database by their username.
     *
     * @param username username to search for
     * @return the User if found, otherwise null
     */
    @Override
    public User findByUsername(String username) {
        log.debug("🔍 Searching for user by username='{}'", username);
        return loginRepo.findByUsername(username);
    }
}
