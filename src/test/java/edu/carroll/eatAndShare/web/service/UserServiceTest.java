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
import java.util.Set;

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
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    private static final String USERNAME = "TestUser";
    private static final String PASSWORD = "iamTesting";
    private static final String EMAIL = "iamyouremail@gmail.com";
    private static final String FIRSTNAME = "TestFirstName";
    private static final String LASTNAME = "TestLastName";

    private  User testUser = new User();

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
        assertNotEquals(PASSWORD, saved.getPassword());
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

        List<User> users = new ArrayList<>();
        users.add(userService.findByUsername("Lokkki"));
        users.add(userService.findByUsername("Thooor"));
        List<String> usernames = new ArrayList<>();
        for (User user : users) {
            usernames.add(user.getUsername());
        }
        assertTrue(usernames.contains("Lokkki") &&  usernames.contains("Thooor"));
        // Check if there are two users that are saved
        assertTrue(users.size()==2);
    }

    @Test
    public void savingValidUserWithSymbolsPasswordTest() {
        final String symbolPassword = "II*(#@GGG";
        testUser.setUsername(USERNAME);
        testUser.setPassword(symbolPassword);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        assertDoesNotThrow(() -> userService.saveUser(testUser));
        User saved = userService.findByUsername(USERNAME);

        assertEquals(USERNAME, saved.getUsername());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals(FIRSTNAME, saved.getFirstName());
        assertEquals(LASTNAME, saved.getLastName());
        assertNotEquals(symbolPassword, saved.getPassword());
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
        assertNotEquals(PASSWORD, saved.getPassword());
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
        user2.setUsername(user1.getUsername().toUpperCase());
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

    /**
     * HAPPY PATH: correct username & correct password
     */
    @Test
    public void validateUserValidCredentialsTest() {
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

        UserForm form = new UserForm(USERNAME, PASSWORD);

        assertTrue(userService.validateUser(form),
                "Correct username and password should return true");
        User saved = userService.findByUsername(USERNAME);

        assertEquals(USERNAME, saved.getUsername());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals(FIRSTNAME, saved.getFirstName());
        assertEquals(LASTNAME, saved.getLastName());
        assertNotEquals(PASSWORD, saved.getPassword());
    }

    /**
     * CRAPPY PATH: WRONG password
     */
    @Test
    public void validateUserInvalidPasswordTest() {
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

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
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

        boolean result = userService.updatePassword(USERNAME, PASSWORD, "newStrongPassword");

        assertTrue(result, "Password update should return true");
        assertFalse(userService.validateUser(new UserForm(USERNAME, PASSWORD)),
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
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);
        final String wrongPassword = "wrongPassword";
        boolean result = userService.updatePassword(USERNAME, wrongPassword, "newPassword123");

        assertFalse(result, "Incorrect old password should return false");
        assertTrue(userService.validateUser(new UserForm(USERNAME, PASSWORD)),
                "Old password should still work");
        assertFalse(userService.validateUser(new UserForm(USERNAME, "newPassword123")),
                "New password should not allow login");
    }

    @Test
    public void updatePasswordBlankNewPasswordThrowsTest() {
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(USERNAME, PASSWORD, " ")
        );
        assertEquals(exception.getMessage() , "New password cannot be empty");

        assertTrue(userService.validateUser(new UserForm(USERNAME, PASSWORD)),
                "Old password should still work");
        assertFalse(userService.validateUser(new UserForm(USERNAME, " ")),
                "New password should not allow login");
    }

    @Test
    public void updatePasswordTooShortThrowsTest() {
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(USERNAME, PASSWORD, "3322")
        );
        assertEquals(exception.getMessage() , "New password must be at least 6 characters");

        assertTrue(userService.validateUser(new UserForm(USERNAME, PASSWORD)),
                "Old password should still work");
        assertFalse(userService.validateUser(new UserForm(USERNAME, " ")),
                "New password should not allow login");
    }

    @Test
    public void updatePasswordContainsSpacesThrowsTest() {
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(USERNAME, PASSWORD, "33  22")
        );
        assertEquals(exception.getMessage() , "New password cannot contain spaces");

        assertTrue(userService.validateUser(new UserForm(USERNAME, PASSWORD)),
                "Old password should still work");
        assertFalse(userService.validateUser(new UserForm(USERNAME, " ")),
                "New password should not allow login");
    }


    /*CRAZY PATH — updating multiple times works*/

    @Test
    public void updatePasswordMultipleTimesTest() {
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setEmail(EMAIL);
        testUser.setFirstName(FIRSTNAME);
        testUser.setLastName(LASTNAME);

        userService.saveUser(testUser);

        assertTrue(userService.updatePassword(USERNAME, PASSWORD, "FirstNewPass123"));
        assertFalse(userService.validateUser(new UserForm(USERNAME, PASSWORD)),
                "Old password should not work");
        assertTrue(userService.validateUser(new UserForm(USERNAME, "FirstNewPass123")),
                "New password should allow login");

        assertTrue(userService.updatePassword(USERNAME, "FirstNewPass123", "SecondNewPass456"));
        assertFalse(userService.validateUser(new UserForm(USERNAME, "FirstNewPass123")),
                "Old changed password should not work");
        assertTrue(userService.validateUser(new UserForm(USERNAME, "SecondNewPass456")),
                "New password should allow login");
    }
}
