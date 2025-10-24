/**
 * Filename: UserServiceTest.java
 * Author: Andrias Zelele
 * Date: October 20, 2025
 *
 * Description:
 * This test class verifies the functionality of the UserService implementation
 * in the EatAndShare application. It ensures that user validation and registration
 * logic work as expected — including checking valid/invalid credentials, duplicate
 * usernames, and field validation errors.
 */

package edu.carroll.EatAndShare.web.service;

import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import edu.carroll.EatAndShare.backEnd.model.User;
import edu.carroll.EatAndShare.backEnd.repo.UserRepository;
import edu.carroll.EatAndShare.web.form.UserForm;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Integration tests for {@link UserService}, verifying key user management operations:
 * <ul>
 *   <li>User credential validation (login)</li>
 *   <li>User registration and persistence</li>
 *   <li>Duplicate username detection</li>
 *   <li>Validation of required fields</li>
 * </ul>
 * <p>
 * Tests run on an in-memory H2 database to guarantee data isolation and repeatability.
 */
@Transactional
@SpringBootTest
public class UserServiceTest {

    /** ---------- Constant test data used throughout test cases ---------- */
    private static final String username = "testuser";
    private static final String password = "testpass";
    private static final String email = "testuser@example.com";

    /** Service and dependency injections */
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Baseline test user created before each test for validation checks. */
    private User testUser;

    /**
     * Sets up a baseline test user before each test.
     * Ensures there is always one valid user in the test database
     * for validation and duplicate-check scenarios.
     */
    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setUsername(username);
        testUser.setPassword(passwordEncoder.encode(password));
        testUser.setEmail(email);
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        userRepo.save(testUser);
    }

    /**
     * Verifies that a user with correct username and password
     * is successfully validated by the UserService.
     * <p>
     * Expected: Validation returns true.
     */
    @Test
    public void validateUserSuccessTest() {
        UserForm form = new UserForm();
        form.setUsername(username);
        form.setPassword(password);

        assertTrue("Should validate successfully with correct credentials",
                userService.validateUser(form));
    }

    /**
     * Ensures validation fails when an incorrect password is entered.
     * <p>
     * Expected: Validation returns false.
     */
    @Test
    public void validateUserInvalidPasswordTest() {
        UserForm form = new UserForm();
        form.setUsername(username);
        form.setPassword(password + "wrong");

        assertFalse("Should fail when password is incorrect",
                userService.validateUser(form));
    }

    /**
     * Ensures validation fails when a non-existent username is entered.
     * <p>
     * Expected: Validation returns false.
     */
    @Test
    public void validateUserInvalidUsernameTest() {
        UserForm form = new UserForm();
        form.setUsername(username + "_notexist");
        form.setPassword(password);

        assertFalse("Should fail when username does not exist",
                userService.validateUser(form));
    }

    /**
     * Tests that a new user is successfully saved with encoded password.
     * <p>
     * Checks:
     * - User is persisted in the repository.
     * - Stored password is encoded (not plain text).
     */
    @Test
    public void saveUserSuccessTest() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("newpass");
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");

        userService.saveUser(newUser);

        // Fetch and verify the newly saved user
        User found = userRepo.findByUsername("newuser");
        assertNotNull("Newly registered user should be found in the database", found);
        assertTrue("Stored password should be encoded",
                passwordEncoder.matches("newpass", found.getPassword()));
    }

    /**
     * Verifies that saving a user with an existing username throws
     * an IllegalArgumentException to prevent duplicates.
     * <p>
     * Expected: Exception message equals "Username already exists".
     */
    @Test
    public void saveUserDuplicateUsernameTest() {
        User duplicateUser = new User();
        duplicateUser.setUsername(username);
        duplicateUser.setPassword("anotherpass");
        duplicateUser.setEmail("another@example.com");
        duplicateUser.setFirstName("Dupe");
        duplicateUser.setLastName("User");

        try {
            userService.saveUser(duplicateUser);
            assertFalse("Expected exception for duplicate username but none thrown", true);
        } catch (IllegalArgumentException e) {
            assertEquals("Expected 'Username already exists' message",
                    "Username already exists", e.getMessage());
        }
    }

    /**
     * Ensures that user registration fails when required fields are missing
     * or empty, and an IllegalArgumentException is thrown.
     * <p>
     * Expected: Exception message mentions "cannot be empty".
     */
    @Test
    public void saveUserMissingFieldsTest() {
        User incomplete = new User();
        incomplete.setUsername("");
        incomplete.setEmail("");
        incomplete.setPassword("");

        try {
            userService.saveUser(incomplete);
            assertFalse("Expected exception for missing fields but none thrown", true);
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should mention empty fields",
                    e.getMessage().contains("cannot be empty"));
        }
    }
}
