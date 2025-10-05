package edu.carroll.EatAndShare.web.service;

import java.util.List;

import edu.carroll.EatAndShare.backEnd.model.User;
import edu.carroll.EatAndShare.backEnd.repo.UserRepository;
import edu.carroll.EatAndShare.web.form.UserForm;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository loginRepo;

    public UserServiceImpl(UserRepository loginRepo) {
        this.loginRepo = loginRepo;
    }

    /**
     * Given a loginForm, determine if the information provided is valid, and the user exists in the system.
     *
     * @param userForm - Data containing user login information, such as username and password.
     * @return true if data exists and matches what's on record, false otherwise
     */
    @Override
    public boolean validateUser(UserForm userForm) {
        List<User> users = loginRepo.findByUsernameIgnoreCase(userForm.getUsername());
        if (users.size() != 1)
            return false;

        User u = users.getFirst();

        // Hash the provided password and compare to stored hash
        final String userProvidedHash = Integer.toString(userForm.getPassword().hashCode());
        return u.getPassword().equals(userProvidedHash);
    }

    @Override
    public void saveUser(User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (loginRepo.existsByUsernameIgnoreCase(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (loginRepo.existsByEmailIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (user.getLastName() == null || user.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }


        // hash password
        String hashedPassword = Integer.toString(user.getPassword().hashCode());
        user.setPassword(hashedPassword);

        try {
            loginRepo.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username or email already exists!");
        }
    }



    @Override
    public User findByUsername(String username) {
        return loginRepo.findByUsername(username);
    }

}
//-1164577301