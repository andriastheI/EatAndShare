package edu.carroll.eatAndShare.web.service;

import ch.qos.logback.core.boolex.Matcher;
import edu.carroll.eatAndShare.backEnd.model.User;
import edu.carroll.eatAndShare.backEnd.service.UserService;
import edu.carroll.eatAndShare.backEnd.form.UserForm;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserService.
 * <p>
 * Testing Philosophy:
 * ✅ Happy Path: Valid inputs should work
 * 💩 Crappy Path: Invalid inputs should throw exceptions
 * 🌀 Crazy Path: Edge cases that are weird but should still work
 */

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    private static final String USERNAME = "TestUser";
    private static final String PASSWORD = "iamTesting";
    private static final String EMAIL = "iamyouremail@gmail.com";
    private static final String FIRSTNAME = "TestFirstName";
    private static final String LASTNAME = "TestLastName";

    private static final User testUser = new User();
    private static final PasswordEncoder encoder =  new BCryptPasswordEncoder();

    /*HAPPY PATH TESTS*/

    @Test
    public void savingValidUserTest() {
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        assertDoesNotThrow(() -> userService.saveUser(testUser));
        User saved = userService.findByUsername(USERNAME);
        assertNotNull(saved);

        assertEquals(USERNAME, saved.getUsername());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals(FIRSTNAME, saved.getFirstName());
        assertEquals(LASTNAME, saved.getLastName());
        assertTrue(encoder.matches(PASSWORD, saved.getPassword()));
    }

    @Test
    public void savingTwoUsersTest() {
        List<User> users = new ArrayList<>();

        User user1 = new User();
        user1.setUsername("Lokkki");
        user1.setPassword("iamPassword");
        user1.setEmail("iamyouremail@gmail.com");
        user1.setFirstName("Loki");
        user1.setLastName("High");

        User user2 = new User();
        user2.setUsername("Thooor");
        user2.setPassword("iamPassword2");
        user2.setEmail("yourmom@gmail.com");
        user2.setFirstName("Thor");
        user2.setLastName("Hammer");

        assertDoesNotThrow(() -> {
            userService.saveUser(user1);
            userService.saveUser(user2);
        });

        users.add(userService.findByUsername("Lokkki"));
        users.add(userService.findByUsername("Thooor"));
        // Check if there are two users that are saved
        assertTrue(users.size()==2);
    }

    @Test
    public void savingValidUserWithSymbolsPasswordTest() {
        testUser.setUsername(USERNAME);
        testUser.setPassword("II*(#@GGG");
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        assertDoesNotThrow(() -> userService.saveUser(testUser));
        User saved = userService.findByUsername(USERNAME);

        assertEquals(USERNAME, saved.getUsername());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals(FIRSTNAME, saved.getFirstName());
        assertEquals(LASTNAME, saved.getLastName());
        assertTrue(encoder.matches("II*(#@GGG", saved.getPassword()));
    }


    /*CRAPPY PATH TESTS*/

    @Test
    public void savingExistingUserTest() {
        List<User> users = new ArrayList<>();
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        assertTrue(exception.getMessage().contains("Username already exists"));
        users.add(userService.findByUsername(testUser.getUsername()));
        assertTrue(users.size()==1);
    }

    @Test
    public void savingInvalidPasswordLengthTest() {
        testUser.setUsername(USERNAME);
        testUser.setPassword("short");
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        assertTrue(exception.getMessage().contains("Password cannot be less than 6 characters"));

        assertNull(userService.findByUsername(testUser.getUsername()));
    }

    @Test
    public void savingInvalidPasswordTest() {
        testUser.setUsername(USERNAME);
        testUser.setPassword("sho   rt");
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        assertTrue(exception.getMessage().contains("Password cannot contain a space"));
        assertNull(userService.findByUsername(testUser.getUsername()));
    }

    @Test
    public void savingInvalidUsernameTest() {
        testUser.setUsername("iam_ not correct");
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        assertTrue(exception.getMessage().contains("Username cannot contain a space"));

        assertNull(userService.findByUsername(testUser.getUsername()));
    }

    @Test
    public void savingInvalidUsernameLengthTest() {
        testUser.setUsername("iam_");
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        assertTrue(exception.getMessage().contains("Username cannot be less than 6 characters"));

        assertNull(userService.findByUsername(testUser.getUsername()));
    }

    @Test
    public void savingInvalidFirstnameTest() {
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName("user name");
        testUser.setLastName(LASTNAME);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        assertTrue(exception.getMessage().contains("First name cannot contain a space"));
        assertNull(userService.findByUsername(testUser.getUsername()));
    }

    @Test
    public void savingInvalidLastnameTest() {
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(USERNAME);
        testUser.setLastName("last name");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        assertTrue(exception.getMessage().contains("Last name cannot contain a space"));
        assertNull(userService.findByUsername(testUser.getUsername()));
    }


    /*CRAZY PATH TESTS*/

    @Test
    public void savingCrazyLongUsernameInValidTest() {
        testUser.setUsername("L" + "o".repeat(150));
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(testUser)
        );

        assertTrue(exception.getMessage().contains("Username cannot exceed 10 characters"));

        assertNull(userService.findByUsername(testUser.getUsername()));
    }

    @Test
    public void savingEmojiInFirstnameTest() {
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName("😎Cool");
        testUser.setLastName(LASTNAME);

        assertDoesNotThrow(() -> userService.saveUser(testUser));
        User saved = userService.findByUsername(USERNAME);

        assertEquals(USERNAME, saved.getUsername());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals("😎Cool", saved.getFirstName());
        assertEquals(LASTNAME, saved.getLastName());
        assertTrue(encoder.matches(PASSWORD, saved.getPassword()));

    }

    @Test
    public void savingUsernameCaseSensitiveTest() {
        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setUsername("caseUser");
        user1.setPassword("Pass123!");
        user1.setEmail("a@test.com");
        user1.setFirstName("Case");
        user1.setLastName("User");

        User user2 = new User();
        user2.setUsername("CASEUSER");
        user2.setPassword("Pass124!");
        user2.setEmail("b@test.com");
        user2.setFirstName("Case2");
        user2.setLastName("User2");

        userService.saveUser(user1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(user2)
        );

        assertTrue(exception.getMessage().contains("Username already exists"));
        users.add(userService.findByUsername(user1.getUsername()));
        assertTrue(users.size()==1);
    }
    //TODO: Continue starting at this point
    /**
     * HAPPY PATH: correct username & correct password
     */
    @Test
    public void validateUserValidCredentialsTest() {
        User user2 = new User();
        user2.setUsername("Thooor");
        user2.setPassword("iamPassword2");
        user2.setEmail("yourmom@gmail.com");
        user2.setFirstName("Thor");
        user2.setLastName("Hammer");

        userService.saveUser(user2);

        UserForm form = new UserForm("Thooor", "iamPassword2");

        assertTrue(userService.validateUser(form),
                "Correct username and password should return true");
    }

    /**
     * CRAPPY PATH: WRONG password
     */
    @Test
    public void validateUserInvalidPasswordTest() {
        User user2 = new User();
        user2.setUsername("Thooor");
        user2.setPassword("iamPassword2");
        user2.setEmail("yourmom@gmail.com");
        user2.setFirstName("Thor");
        user2.setLastName("Hammer");

        userService.saveUser(user2);

        UserForm form = new UserForm("Thooor", "wrongPassword");

        assertFalse(userService.validateUser(form),
                "Wrong password should return false");
    }

    /**
     * CRAPPY PATH: username not found
     */
    @Test
    public void validateUserUsernameNotFoundTest() {
        UserForm form = new UserForm("UnknownUser", "iamPassword");

        assertFalse(userService.validateUser(form),
                "Unknown username should return false");
    }

    /**
     * CRAZY PATH: case-insensitive username should still work
     */
    @Test
    public void validateUserCaseInsensitiveUsernameTest() {
        UserForm form = new UserForm("tHoRuSeR", "iamPassword");

        assertFalse(userService.validateUser(form),
                "Username lookup should be case-sensitive");
    }

    /**
     * CRAPPY PATH: Duplicate users (force duplicate manually)
     */
    @Test
    public void validateUserDuplicateUserRecordsTest() {

        // Add another user intentionally with same username different email
        User duplicate = new User();
        duplicate.setUsername("ThorUser"); // SAME NAME
        duplicate.setPassword("iamPassword2");
        duplicate.setEmail("duplicate@avengers.com");
        duplicate.setFirstName("Copy");
        duplicate.setLastName("User");

        userService.saveUser(duplicate);

        UserForm form = new UserForm("ThorUser", "iamPassword");

        assertFalse(userService.validateUser(form),
                "Duplicate records should return false");
    }


    /* Update Password Testing */

    /*HAPPY PATH — password updates successfully */
    @Test
    public void updatePasswordValidTest() {
        boolean result = userService.updatePassword(USERNAME, "originalPassword", "newStrongPassword");

        assertTrue(result, "Password update should return true");
        assertFalse(userService.validateUser(new UserForm(USERNAME, "originalPassword")),
                "Old password should no longer work");
        assertTrue(userService.validateUser(new UserForm(USERNAME, "newStrongPassword")),
                "New password should allow login");
    }

    /*CRAPPY PATH TESTS — problems should be handled*/

    @Test
    public void updatePasswordUserNotFoundTest() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword("UnknownUser", "password", "newPass")
        );
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    public void updatePasswordIncorrectOldPasswordTest() {
        boolean result = userService.updatePassword(USERNAME, "wrongOldPassword", "newPassword123");

        assertFalse(result, "Incorrect old password should return false");
    }

    @Test
    public void updatePasswordBlankNewPasswordThrowsTest() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(USERNAME, "originalPassword", " ")
        );
        assertTrue(exception.getMessage().contains("Password cannot be empty"));
    }

    @Test
    public void updatePasswordTooShortThrowsTest() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(USERNAME, "originalPassword", "123")
        );
        assertTrue(exception.getMessage().contains("Password must be at least 6 characters"));
    }

    @Test
    public void updatePasswordContainsSpacesThrowsTest() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(USERNAME, "originalPassword", "New Pass")
        );

        assertTrue(exception.getMessage().contains("Password cannot contain spaces"));
    }


    /*CRAZY PATH — updating multiple times works*/

    @Test
    public void updatePasswordMultipleTimesTest() {

        assertTrue(userService.updatePassword(USERNAME, "originalPassword", "FirstNewPass123"));
        assertTrue(userService.updatePassword(USERNAME, "FirstNewPass123", "SecondNewPass456"));

        assertTrue(userService.validateUser(new UserForm(USERNAME, "SecondNewPass456")));
    }
}
