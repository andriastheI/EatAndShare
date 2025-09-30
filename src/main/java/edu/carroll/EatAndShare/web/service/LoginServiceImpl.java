package edu.carroll.EatAndShare.web.service;

import java.util.List;

import edu.carroll.EatAndShare.backEnd.model.Login;
import edu.carroll.EatAndShare.backEnd.repo.LoginRepository;
import edu.carroll.EatAndShare.web.form.LoginForm;
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
        if (!loginRepo.findByUsernameIgnoreCase(login.getUsername()).isEmpty()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Hash before saving
        String hashedPassword = Integer.toString(login.getPassword().hashCode());
        login.setPassword(hashedPassword);

        loginRepo.save(login);
    }

    @Override
    public Login findByUsername(String username) {
        return loginRepo.findByUsername(username);
    }

}
//-1164577301