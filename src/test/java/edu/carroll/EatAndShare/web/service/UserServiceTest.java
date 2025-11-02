package edu.carroll.EatAndShare.web.service;

import edu.carroll.EatAndShare.backEnd.model.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserServiceImpl userSerImp;


    /** ---------- Constant test data used throughout test cases ---------- */
    private static final String USERNAME = "testuser";
    private static final String EMAIL = "testuser@example.com";

    /** Baseline test user created before each test for validation checks. */
    private User testUser;

    /**
     * Creates and saves a test user in the in-memory database
     * before each test executes. This ensures that valid user data
     * exists for linking recipes.
     */
    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setUsername(USERNAME);
        testUser.setPassword("password");
        testUser.setEmail(EMAIL);
        testUser.setFirstName("Test");
        testUser.setLastName("Chef");
        userSerImp.saveUser(testUser);
    }

    @Test
    public void savingUserTest(){
        User user  = new User();
        user.setUsername("Lokkki");
        user.setPassword("iamPassword");
        user.setEmail("iamyouremail@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("High");
        userSerImp.saveUser(user);

        User userCheck = userSerImp.findByUsername("Lokkki");
        boolean  result = userCheck.equals(user);
        assertTrue(result, "The users should be the same");
    }

    @Test
    public void savingExistingUserTest() {
        User user = new User();
        user.setUsername("Lokkki");
        user.setPassword("iamPassword");
        user.setEmail("iamyouremail@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("High");

        userSerImp.saveUser(user);

        // Act + Assert — second save should throw exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSerImp.saveUser(user),
                "Saving a user with an existing username should throw an error"
        );

        assertTrue(
                exception.getMessage().contains("Username already exists"),
                "Exception message should mention duplicate username"
        );
    }

    @Test
    public void savingTwoUsersTest() {
        User user = new User();
        user.setUsername("Lokkki");
        user.setPassword("iamPassword");
        user.setEmail("iamyouremail@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("High");

        User user2 = new User();
        user2.setUsername("Thooor");
        user2.setPassword("iamPassword2");
        user2.setEmail("yourmom@gmail.com");
        user2.setFirstName("Thor");
        user2.setLastName("Hammer");

        assertDoesNotThrow(() -> {
            userSerImp.saveUser(user);
            userSerImp.saveUser(user2);
        }, "Saving 2 UNIQUE users should not throw an exception");
    }

    @Test
    public void savingInvalidPasswordLengthTest() {
        User user = new User();
        user.setUsername("Lokkki"); // <-- contains space
        user.setPassword("iaord");
        user.setEmail("iamyouremail@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("High");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSerImp.saveUser(user),
                "Password less than 6 characters should throw an exception"
        );

        assertTrue(
                exception.getMessage().contains("Password cannot be less than 6 characters"),
                "Exception message should mention invalid Password Length"
        );
    }

    @Test
    public void savingInvalidPasswordTest() {
        User user = new User();
        user.setUsername("Lokkki"); // <-- contains space
        user.setPassword("iam  Password");
        user.setEmail("iamyouremail@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("High");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSerImp.saveUser(user),
                "Password containing spaces should throw an exception"
        );

        assertTrue(
                exception.getMessage().contains("Password cannot contain a space"),
                "Exception message should mention invalid Password"
        );
    }

    @Test
    public void savingInvalidUsernameTest() {
        User user = new User();
        user.setUsername("Lo kki"); // <-- contains space
        user.setPassword("iamPassword");
        user.setEmail("iamyouremail@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("High");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSerImp.saveUser(user),
                "Username containing spaces  should throw an exception"
        );

        assertTrue(
                exception.getMessage().contains("Username cannot contain a space"),
                "Exception message should mention invalid username"
        );
    }

    @Test
    public void savingInvalidUsernameLengthTest() {
        User user = new User();
        user.setUsername("Loki"); // <-- contains space
        user.setPassword("iamPassword");
        user.setEmail("iamyouremail@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("High");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSerImp.saveUser(user),
                "Username containing 6 characters should throw an exception"
        );

        assertTrue(
                exception.getMessage().contains("Username cannot be less than 6 characters"),
                "Exception message should mention invalid username length"
        );
    }

    @Test
    public void savingInvalidFirstnameTest() {
        User user = new User();
        user.setUsername("Lokkki"); // <-- contains space
        user.setPassword("iamPassword");
        user.setEmail("iamyouremail@gmail.com");
        user.setFirstName("Lo ki");
        user.setLastName("High");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSerImp.saveUser(user),
                "Firstname containing spaces  should throw an exception"
        );

        assertTrue(
                exception.getMessage().contains("Firstname cannot contain a space"),
                "Exception message should mention invalid firstname"
        );
    }
    @Test
    public void savingInvalidLastnameTest() {
        User user = new User();
        user.setUsername("Lokkki"); // <-- contains space
        user.setPassword("iamPassword");
        user.setEmail("iamyouremail@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("Hi gh");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSerImp.saveUser(user),
                "Lastname containing spaces  should throw an exception"
        );

        assertTrue(
                exception.getMessage().contains("Lastname cannot contain a space"),
                "Exception message should mention invalid lastname"
        );
    }
    @Test
    public void savingInvalidEmailTest() {
        User user = new User();
        user.setUsername("Lokkki"); // <-- contains space
        user.setPassword("iamPassword");
        user.setEmail("iamyouremail@gmail.com");
        user.setFirstName("Loki");
        user.setLastName("Hi gh");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userSerImp.saveUser(user),
                "Lastname containing spaces  should throw an exception"
        );

        assertTrue(
                exception.getMessage().contains("Lastname cannot contain a space"),
                "Exception message should mention invalid lastname"
        );
    }



}