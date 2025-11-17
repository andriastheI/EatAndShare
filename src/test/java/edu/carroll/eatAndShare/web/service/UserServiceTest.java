package edu.carroll.eatAndShare.web.service;

import edu.carroll.eatAndShare.backEnd.model.User;
import edu.carroll.eatAndShare.backEnd.service.UserService;
import edu.carroll.eatAndShare.backEnd.form.UserForm;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserService.
 * <p>
 * Testing Philosophy:
 * ✅ Happy Path: Valid inputs should work
 * 💩 Crappy Path: Invalid inputs should throw exceptions
 * 🌀 Crazy Path: Edge cases that are weird but should still work
 */
@Transactional
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    private static final String USERNAME = "TestUser";

    @BeforeEach
    public void setup() {
        User user = new User();
        user.setUsername(USERNAME);
        user.setPassword("originalPassword");
        user.setEmail("user@test.com");
        user.setFirstName("Test");
        user.setLastName("User");

        userService.saveUser(user);     // Password will be encoded here
    }
    /*HAPPY PATH TESTS*/

    @Test
    public void savingValidUserTest() {
        User user = new User();
        user.setUsername("Lokkki");
        user.setPassword("iamPassword");
        user.setEmail("iamyouremail@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("High");

        userService.saveUser(user);
        User userCheck = userService.findByUsername("Lokkki");

        assertNotNull(userCheck);
        assertEquals(user.getUsername(), userCheck.getUsername());
    }

    @Test
    public void savingTwoUsersTest() {
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

        assertNotNull(userService.findByUsername("Lokkki"));
        assertNotNull(userService.findByUsername("Thooor"));
    }

    @Test
    public void savingValidUserWithSymbolsPasswordTest() {
        User user = new User();
        user.setUsername("SuperUser");
        user.setPassword("P@s$W0rd!#*");
        user.setEmail("super@hero.com");
        user.setFirstName("Symbolic");
        user.setLastName("Hero");

        assertDoesNotThrow(() -> userService.saveUser(user));
        assertEquals(user.getPassword(), user.getPassword());
    }


    /*CRAPPY PATH TESTS*/

    @Test
    public void savingExistingUserTest() {
        User user = new User();
        user.setUsername("Lokkki");
        user.setPassword("iamPassword");
        user.setEmail("iamyouremail@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("High");

        userService.saveUser(user);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(user)
        );

        assertTrue(exception.getMessage().contains("Username already exists"));
    }

    @Test
    public void savingInvalidPasswordLengthTest() {
        User user = new User();
        user.setUsername("Lokkki");
        user.setPassword("short");
        user.setEmail("email@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("High");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(user)
        );

        assertTrue(exception.getMessage().contains("Password cannot be less than 6 characters"));
    }

    @Test
    public void savingInvalidPasswordTest() {
        User user = new User();
        user.setUsername("Lokkki");
        user.setPassword("iam Pass");
        user.setEmail("email@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("High");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(user)
        );

        assertTrue(exception.getMessage().contains("Password cannot contain a space"));
    }

    @Test
    public void savingInvalidUsernameTest() {
        User user = new User();
        user.setUsername("Lo kki");
        user.setPassword("iamPassword");
        user.setEmail("email@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("High");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(user)
        );

        assertTrue(exception.getMessage().contains("Username cannot contain a space"));
    }

    @Test
    public void savingInvalidUsernameLengthTest() {
        User user = new User();
        user.setUsername("Loki");
        user.setPassword("iamPassword");
        user.setEmail("email@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("High");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(user)
        );

        assertTrue(exception.getMessage().contains("Username cannot be less than 6 characters"));
    }

    @Test
    public void savingInvalidFirstnameTest() {
        User user = new User();
        user.setUsername("Lokkki");
        user.setPassword("iamPassword");
        user.setEmail("email@gmail.com");
        user.setFirstName("Lo ki");
        user.setLastName("High");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(user)
        );

        assertTrue(exception.getMessage().contains("Firstname cannot contain a space"));
    }

    @Test
    public void savingInvalidLastnameTest() {
        User user = new User();
        user.setUsername("Lokkki");
        user.setPassword("iamPassword");
        user.setEmail("email@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("Hi gh");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.saveUser(user)
        );

        assertTrue(exception.getMessage().contains("Lastname cannot contain a space"));
    }


    /*CRAZY PATH TESTS*/

    @Test
    public void savingCrazyLongUsernameValidTest() {
        User user = new User();
        user.setUsername("L" + "o".repeat(150));
        user.setPassword("iamPassword");
        user.setEmail("crazy@test.com");
        user.setFirstName("Crazy");
        user.setLastName("User");

        assertDoesNotThrow(() -> userService.saveUser(user));
    }

    @Test
    public void savingEmojiInFirstnameTest() {
        User user = new User();
        user.setUsername("EmojiUser");
        user.setPassword("iamPassword");
        user.setEmail("emoji@test.com");
        user.setFirstName("😎Cool");
        user.setLastName("User");

        assertDoesNotThrow(() -> userService.saveUser(user));
    }

    @Test
    public void savingUsernameCaseSensitiveTest() {
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
    }


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
