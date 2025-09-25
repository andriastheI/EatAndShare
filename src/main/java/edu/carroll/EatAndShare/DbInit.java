package edu.carroll.EatAndShare;

import java.util.List;

import edu.carroll.EatAndShare.backEnd.model.Login;
import edu.carroll.EatAndShare.backEnd.repo.LoginRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

// This class optionally pre-populates the database with login data.  In
// a real application, this would be done with a completely different
// method.
@Component
public class DbInit {
    // XXX - This is wrong on so many levels....
    private static final String defaultUsername = "cs341user";
    private static final String defaultPassHash = "-1164577301";
    private static final String defaultEmail = "yourmom@gmail.com";
    private final LoginRepository loginRepo;

    public DbInit(LoginRepository loginRepo) {
        this.loginRepo = loginRepo;
    }

    // invoked during application startup
    @PostConstruct
    public void loadData() {
        // If the user doesn't exist in the database, populate it
        final List<Login> defaultUsers = loginRepo.findByUsernameIgnoreCase(defaultUsername);
        if (defaultUsers.isEmpty()) {
            Login defaultUser = new Login();
            defaultUser.setUsername(defaultUsername);
            defaultUser.setPassword(defaultPassHash);
            defaultUser.setEmail(defaultEmail);
            loginRepo.save(defaultUser);
        }
    }
}