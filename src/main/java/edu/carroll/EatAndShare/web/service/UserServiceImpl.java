package edu.carroll.EatAndShare.web.service;

import java.util.List;

import edu.carroll.EatAndShare.backEnd.model.User;
import edu.carroll.EatAndShare.backEnd.repo.UserRepository;
import edu.carroll.EatAndShare.web.form.UserForm;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Filename: UserServiceImpl.java
 * Author: Andrias Zelele
 * Date: October 20, 2025
 *
 * Description:
 * This service class provides the implementation for user-related operations within
 * the EatAndShare web application. It handles user authentication, validation,
 * password encryption, and registration logic. The class interacts with the
 * UserRepository to perform database operations such as saving and retrieving
 * user information. It also ensures data integrity by validating input fields
 * and preventing duplicate usernames or emails.
 */
@Service
public class UserServiceImpl implements UserService {

    /** Repository for accessing and managing user data in the database. */
    private final UserRepository loginRepo;

    /** Used to encode and verify passwords securely. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a UserServiceImpl with dependencies injected.
     *
     * @param loginRepo the repository used to perform CRUD operations on users
     * @param passwordEncoder the encoder used for hashing passwords
     */
    public UserServiceImpl(UserRepository loginRepo, PasswordEncoder passwordEncoder) {
        this.loginRepo = loginRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Validates a user's login information by checking if the username exists
     * and if the provided password matches the stored hashed password.
     *
     * @param userForm the form object containing login data such as username and password
     * @return true if the username exists and the password matches; false otherwise
     */
    @Override
    public boolean validateUser(UserForm userForm) {
        // Retrieve list of users with the provided username (case-insensitive)
        List<User> users = loginRepo.findByUsernameIgnoreCase(userForm.getUsername());
        if (users.size() != 1)
            return false;

        // Get the user object from the list
        User u = users.getFirst();

        // Compare the provided password with the hashed password stored in the database
        return passwordEncoder.matches(userForm.getPassword(), u.getPassword());
    }

    /**
     * Saves a new user into the database after performing validation checks
     * such as ensuring unique username and email, and encoding the password.
     *
     * @param user the user object containing registration information
     * @throws IllegalArgumentException if any required field is missing or violates constraints
     */
    @Override
    public void saveUser(User user) {
        // Validate that required fields are not null or blank
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Check for duplicate username or email
        if (loginRepo.existsByUsernameIgnoreCase(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (loginRepo.existsByEmailIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Validate first and last name
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (user.getLastName() == null || user.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }

        // Hash the password before saving to ensure secure storage
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        // Attempt to save user and handle integrity violations (duplicate username/email)
        try {
            loginRepo.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username or email already exists!");
        }
    }

    /**
     * Finds a user in the database by their username.
     *
     * @param username the username of the user to search for
     * @return the User object if found, otherwise null
     */
    @Override
    public User findByUsername(String username) {
        return loginRepo.findByUsername(username);
    }

}
