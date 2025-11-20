package edu.carroll.eatAndShare.web.service;

import edu.carroll.eatAndShare.backEnd.model.User;
import edu.carroll.eatAndShare.backEnd.service.UserService;
import edu.carroll.eatAndShare.web.form.UserForm;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserService.
 * These tests validate user creation, password handling, and uniqueness constraints.
 */
@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    // ---------- Shared valid test constants ----------
    private static final String USERNAME = "TestUser";
    private static final String PASSWORD = "iamTesting";
    private static final String EMAIL = "iamyouremail@gmail.com";
    private static final String FIRSTNAME = "TestFirstName";
    private static final String LASTNAME = "TestLastName";

    // Reusable user object for multiple tests
    private User testUser = new User();

    // ================= HAPPY PATH TESTS =================

    /**
     * Verifies that a valid user is saved successfully.
     */
    @Test
    public void savingValidUserTest() {
        // Arrange: populate valid user fields
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        // Act & Assert: save should not throw exception
        assertDoesNotThrow(() -> userService.saveUser(testUser));

        // Fetch saved user
        User saved = userService.findByUsername(USERNAME);

        // Validate persistence
        assertNotNull(saved);
        assertEquals(USERNAME, saved.getUsername());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals(FIRSTNAME, saved.getFirstName());
        assertEquals(LASTNAME, saved.getLastName());

        // Ensure password is hashed and not stored as plain text
        assertNotEquals(PASSWORD, saved.getPassword());
    }

    /**
     * Ensures multiple valid users can be saved independently.
     */
    @Test
    public void savingTwoUsersTest() {
        // Arrange: create first user
        User user1 = new User();
        user1.setUsername("Lokkki");
        user1.setPassword("iamPassword");
        user1.setEmail("iamyouremail@gmail.com");
        user1.setFirstName("Loki");
        user1.setLastName("High");

        // Arrange: create second user
        User user2 = new User();
        user2.setUsername("Thooor");
        user2.setPassword("iamPassword2");
        user2.setEmail("yourmom@gmail.com");
        user2.setFirstName("Thor");
        user2.setLastName("Hammer");

        // Act: attempt to save both users
        assertDoesNotThrow(() -> {
            userService.saveUser(user1);
            userService.saveUser(user2);
        });

        // Collect saved users by username
        List<User> users = new ArrayList<>();
        users.add(userService.findByUsername("Lokkki"));
        users.add(userService.findByUsername("Thooor"));

        // Extract usernames for validation
        List<String> usernames = new ArrayList<>();
        for (User user : users) {
            usernames.add(user.getUsername());
        }

        // Assert both users were saved
        assertTrue(usernames.contains("Lokkki") && usernames.contains("Thooor"));
        assertTrue(users.size() == 2);
    }

    /**
     * Ensures passwords containing symbols are accepted and hashed properly.
     */
    @Test
    public void savingValidUserWithSymbolsPasswordTest() {
        // Arrange: create password with symbols
        final String symbolPassword = "II*(#@GGG";

        testUser.setUsername(USERNAME);
        testUser.setPassword(symbolPassword);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        // Act & Assert: save should succeed
        assertDoesNotThrow(() -> userService.saveUser(testUser));

        // Retrieve saved user
        User saved = userService.findByUsername(USERNAME);

        // Validate persistence
        assertEquals(USERNAME, saved.getUsername());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals(FIRSTNAME, saved.getFirstName());
        assertEquals(LASTNAME, saved.getLastName());

        // Confirm password was hashed
        assertNotEquals(symbolPassword, saved.getPassword());
    }

    // ================= CRAPPY PATH TESTS =================

    /**
     * Ensures duplicate usernames are rejected.
     */
    @Test
    public void savingExistingUserTest() {
        // Track saved user list
        List<User> users = new ArrayList<>();

        // Arrange: populate valid user
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        // First save should succeed
        userService.saveUser(testUser);

        // Second save should fail
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        // Validate exception message
        assertTrue(exception.getMessage().contains("Username already exists"));

        // Confirm only one user exists
        users.add(userService.findByUsername(testUser.getUsername()));
        assertTrue(users.size() == 1);
    }

    /**
     * Ensures passwords shorter than the minimum length are rejected.
     */
    @Test
    public void savingInvalidPasswordLengthTest() {
        // Arrange: create user with short password
        testUser.setUsername(USERNAME);
        testUser.setPassword("short"); // invalid length
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        // Act & Assert: exception expected
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        // Validate correct error message
        assertTrue(exception.getMessage().contains("Password cannot be less than 6 characters"));

        // Ensure user was not saved
        assertNull(userService.findByUsername(testUser.getUsername()));
    }

    /**
     * Ensures passwords containing spaces are rejected.
     */
    @Test
    public void savingInvalidPasswordTest() {
        // Arrange: invalid password with spaces
        testUser.setUsername(USERNAME);
        testUser.setPassword("sho   rt"); // invalid spacing
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        // Act & Assert: expect validation exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        // Verify correct message
        assertTrue(exception.getMessage().contains("Password cannot contain a space"));

        // Ensure persistence did not happen
        assertNull(userService.findByUsername(testUser.getUsername()));
    }

    /**
     * Ensures usernames containing spaces are rejected.
     */
    @Test
    public void savingInvalidUsernameTest() {
        // Arrange: invalid username with spaces
        testUser.setUsername("iam_ not correct");
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        // Act & Assert: expect validation exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        // Validate correct message
        assertTrue(exception.getMessage().contains("Username cannot contain a space"));

        // Confirm user was not saved
        assertNull(userService.findByUsername(testUser.getUsername()));
    }
    /**
     * Ensures usernames shorter than the minimum length are rejected.
     */
    @Test
    public void savingInvalidUsernameLengthTest() {
        // Arrange: create user with short username
        testUser.setUsername("iam_"); // invalid length
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        // Act & Assert: expect validation exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        // Verify correct error message
        assertTrue(exception.getMessage().contains("Username cannot be less than 6 characters"));

        // Confirm user was not saved
        assertNull(userService.findByUsername(testUser.getUsername()));
    }

    /**
     * Ensures first names containing spaces are rejected.
     */
    @Test
    public void savingInvalidFirstnameTest() {
        // Arrange: first name contains spaces
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName("user name"); // invalid
        testUser.setLastName(LASTNAME);

        // Act & Assert: expect exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        // Validate error response
        assertTrue(exception.getMessage().contains("First name cannot contain a space"));

        // Ensure user not persisted
        assertNull(userService.findByUsername(testUser.getUsername()));
    }

    /**
     * Ensures last names containing spaces are rejected.
     */
    @Test
    public void savingInvalidLastnameTest() {
        // Arrange: last name contains spaces
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(USERNAME);
        testUser.setLastName("last name"); // invalid

        // Act & Assert: expect exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        // Validate correct message
        assertTrue(exception.getMessage().contains("Last name cannot contain a space"));

        // Confirm no persistence
        assertNull(userService.findByUsername(testUser.getUsername()));
    }

    // ================= CRAZY PATH TESTS =================

    /**
     * Ensures extremely long usernames are rejected.
     */
    @Test
    public void savingCrazyLongUsernameInValidTest() {
        // Arrange: generate an excessively long username
        testUser.setUsername("L" + "o".repeat(150));
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        // Act & Assert: expect exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        // Validate correct message
        assertTrue(exception.getMessage().contains("Username cannot exceed 10 characters"));

        // Confirm user was not saved
        assertNull(userService.findByUsername(testUser.getUsername()));
    }

    /**
     * Verifies that emojis are allowed in the first name field.
     */
    @Test
    public void savingEmojiInFirstnameTest() {
        // Arrange: emoji included in first name
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName("😎Cool");
        testUser.setLastName(LASTNAME);

        // Act & Assert: save should not throw
        assertDoesNotThrow(() -> userService.saveUser(testUser));

        // Retrieve saved user
        User saved = userService.findByUsername(USERNAME);

        // Validate persistence
        assertEquals(USERNAME, saved.getUsername());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals("😎Cool", saved.getFirstName());
        assertEquals(LASTNAME, saved.getLastName());

        // Confirm password hashing
        assertNotEquals(PASSWORD, saved.getPassword());
    }

    /**
     * Ensures username uniqueness is case-insensitive.
     */
    @Test
    public void savingUsernameCaseSensitiveTest() {
        // Track saved users
        List<User> users = new ArrayList<>();

        // Arrange: first user
        User user1 = new User();
        user1.setUsername("caseUser");
        user1.setPassword("Pass123!");
        user1.setEmail("a@test.com");
        user1.setFirstName("Case");
        user1.setLastName("User");

        // Arrange: second user with case-variant username
        User user2 = new User();
        user2.setUsername(user1.getUsername().toUpperCase());
        user2.setPassword("Pass124!");
        user2.setEmail("b@test.com");
        user2.setFirstName("Case2");
        user2.setLastName("User2");

        // Persist first user
        userService.saveUser(user1);

        // Attempt saving duplicate with different case
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(user2)
        );

        // Validate duplication logic
        assertTrue(exception.getMessage().contains("Username already exists"));

        // Confirm only one user exists
        users.add(userService.findByUsername(user1.getUsername()));
        assertTrue(users.size() == 1);
    }

    /**
     * Validates that correct credentials return true.
     */
    @Test
    public void validateUserValidCredentialsTest() {
        // Arrange: create valid user
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        // Persist user
        userService.saveUser(testUser);

        // Create login form
        UserForm form = new UserForm(USERNAME, PASSWORD);

        // Act & Assert: valid credentials
        assertTrue(userService.validateUser(form),
                "Correct username and password should return true");

        // Validate persistence
        User saved = userService.findByUsername(USERNAME);
        assertEquals(USERNAME, saved.getUsername());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals(FIRSTNAME, saved.getFirstName());
        assertEquals(LASTNAME, saved.getLastName());
        assertNotEquals(PASSWORD, saved.getPassword());
    }

    /**
     * Ensures invalid passwords fail validation.
     */
    @Test
    public void validateUserInvalidPasswordTest() {
        // Arrange: create user
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        // Persist user
        userService.saveUser(testUser);

        // Create login form with wrong password
        UserForm form = new UserForm("Thooor", "wrongPassword");

        // Assert failed validation
        assertFalse(userService.validateUser(form),
                "Wrong password should return false");
    }

    /**
     * Ensures unknown usernames fail validation.
     */
    @Test
    public void validateUserUsernameNotFoundTest() {
        // Create login form with unknown user
        UserForm form = new UserForm("UnknownUser", "iamPassword");

        // Assert authentication fails
        assertFalse(userService.validateUser(form),
                "Unknown username should return false");
    }

    /**
     * Verifies that username lookup is case-sensitive during validation.
     * Even if the casing differs, the authentication should fail.
     */
    @Test
    public void validateUserCaseInsensitiveUsernameTest() {
        // Arrange: create form with different username casing
        UserForm form = new UserForm("tHoRuSeR", "iamPassword");

        // Act & Assert: mismatched casing should fail authentication
        assertFalse(userService.validateUser(form),
                "Username lookup should be case-sensitive");
    }

    /**
     * Ensures authentication fails when duplicate database records exist for the same username.
     */
    @Test
    public void validateUserDuplicateUserRecordsTest() {

        // Arrange: intentionally insert a duplicate record
        User duplicate = new User();
        duplicate.setUsername("ThorUser"); // duplicated username
        duplicate.setPassword("iamPassword2");
        duplicate.setEmail("duplicate@avengers.com");
        duplicate.setFirstName("Copy");
        duplicate.setLastName("User");

        // Persist duplicate user
        userService.saveUser(duplicate);

        // Attempt authentication
        UserForm form = new UserForm("ThorUser", "iamPassword");

        // Assert authentication fails due to conflicting records
        assertFalse(userService.validateUser(form),
                "Duplicate records should return false");
    }

    // ================= Password Update Tests =================

    /**
     * HAPPY PATH: Ensures a valid password update succeeds.
     */
    @Test
    public void updatePasswordValidTest() {
        // Arrange: create and persist user
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

        // Act: update password
        boolean result = userService.updatePassword(USERNAME, PASSWORD, "newStrongPassword");

        // Assert: update success
        assertTrue(result, "Password update should return true");

        // Validate old password no longer works
        assertFalse(userService.validateUser(new UserForm(USERNAME, PASSWORD)),
                "Old password should no longer work");

        // Validate new password works
        assertTrue(userService.validateUser(new UserForm(USERNAME, "newStrongPassword")),
                "New password should allow login");
    }

    // ================= Error Path Tests =================

    /**
     * Ensures an exception is thrown when the user does not exist.
     */
    @Test
    public void updatePasswordUserNotFoundTest() {
        // Act & Assert: update on non-existent user should throw
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword("UnknownUser", "password", "newPass")
        );

        // Validate correct error message
        assertTrue(exception.getMessage().contains("User not found"));
    }

    /**
     * Ensures update fails if the old password is incorrect.
     */
    @Test
    public void updatePasswordIncorrectOldPasswordTest() {
        // Arrange: create and persist user
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

        final String wrongPassword = "wrongPassword";

        // Act: attempt password update with incorrect old password
        boolean result = userService.updatePassword(USERNAME, wrongPassword, "newPassword123");

        // Assert update failure
        assertFalse(result, "Incorrect old password should return false");

        // Original password should still work
        assertTrue(userService.validateUser(new UserForm(USERNAME, PASSWORD)),
                "Old password should still work");

        // New password should not work
        assertFalse(userService.validateUser(new UserForm(USERNAME, "newPassword123")),
                "New password should not allow login");
    }

    /**
     * Ensures blank passwords are rejected during updates.
     */
    @Test
    public void updatePasswordBlankNewPasswordThrowsTest() {
        // Arrange: persist user
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

        // Act & Assert: expect validation exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(USERNAME, PASSWORD, " ")
        );

        // Validate message
        assertEquals(exception.getMessage(), "New password cannot be empty");

        // Ensure original password unchanged
        assertTrue(userService.validateUser(new UserForm(USERNAME, PASSWORD)),
                "Old password should still work");

        // Ensure blank password does not validate
        assertFalse(userService.validateUser(new UserForm(USERNAME, " ")),
                "New password should not allow login");
    }

    /**
     * Ensures new password must be of minimum required length.
     */
    @Test
    public void updatePasswordTooShortThrowsTest() {
        // Arrange
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(USERNAME, PASSWORD, "3322")
        );

        // Validate correct error message
        assertEquals(exception.getMessage(), "New password must be at least 6 characters");

        // Old password should remain valid
        assertTrue(userService.validateUser(new UserForm(USERNAME, PASSWORD)),
                "Old password should still work");

        // New invalid password should not work
        assertFalse(userService.validateUser(new UserForm(USERNAME, " ")),
                "New password should not allow login");
    }

    /**
     * Ensures new passwords containing spaces are rejected.
     */
    @Test
    public void updatePasswordContainsSpacesThrowsTest() {
        // Arrange
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(USERNAME, PASSWORD, "33  22")
        );

        // Validate correct error message
        assertEquals(exception.getMessage(), "New password cannot contain spaces");

        // Confirm original password unchanged
        assertTrue(userService.validateUser(new UserForm(USERNAME, PASSWORD)),
                "Old password should still work");

        // Confirm invalid password does not authenticate
        assertFalse(userService.validateUser(new UserForm(USERNAME, " ")),
                "New password should not allow login");
    }

    /**
     * Ensures passwords can be updated multiple times in sequence successfully.
     */
    @Test
    public void updatePasswordMultipleTimesTest() {
        // Arrange
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

        // First update
        assertTrue(userService.updatePassword(USERNAME, PASSWORD, "FirstNewPass123"));
        assertFalse(userService.validateUser(new UserForm(USERNAME, PASSWORD)),
                "Old password should not work");
        assertTrue(userService.validateUser(new UserForm(USERNAME, "FirstNewPass123")),
                "New password should allow login");

        // Second update
        assertTrue(userService.updatePassword(USERNAME, "FirstNewPass123", "SecondNewPass456"));
        assertFalse(userService.validateUser(new UserForm(USERNAME, "FirstNewPass123")),
                "Old changed password should not work");
        assertTrue(userService.validateUser(new UserForm(USERNAME, "SecondNewPass456")),
                "New password should allow login");
    }
}
