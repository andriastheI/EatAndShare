package edu.carroll.EatAndShare.web.service;

import java.util.List;

import edu.carroll.EatAndShare.backEnd.model.Login;
import edu.carroll.EatAndShare.backEnd.repo.LoginRepository;
import edu.carroll.EatAndShare.web.form.LoginForm;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
    private final LoginRepository loginRepo;

    public LoginServiceImpl(LoginRepository loginRepo) {
        this.loginRepo = loginRepo;
    }

    /**
     * Given a loginForm, determine if the information provided is valid, and the user exists in the system.
     *
     * @param loginForm - Data containing user login information, such as username and password.
     * @return true if data exists and matches what's on record, false otherwise
     */
    @Override
    public boolean validateUser(LoginForm loginForm) {
        List<Login> users = loginRepo.findByUsernameIgnoreCase(loginForm.getUsername());
        if (users.size() != 1)
            return false;

        Login u = users.getFirst();

        // Hash the provided password and compare to stored hash
        final String userProvidedHash = Integer.toString(loginForm.getPassword().hashCode());
        return u.getPassword().equals(userProvidedHash);
    }

    @Override
    public void saveUser(Login login) {
        if (login.getUsername() == null || login.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (login.getEmail() == null || login.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (login.getPassword() == null || login.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (loginRepo.existsByUsernameIgnoreCase(login.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (loginRepo.existsByEmailIgnoreCase(login.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (login.getFirstName() == null || login.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (login.getLastName() == null || login.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }


        // hash password
        String hashedPassword = Integer.toString(login.getPassword().hashCode());
        login.setPassword(hashedPassword);

        try {
            loginRepo.save(login);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username or email already exists!");
        }
    }



    @Override
    public Login findByUsername(String username) {
        return loginRepo.findByUsername(username);
    }

}
//-1164577301