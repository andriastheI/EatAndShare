package edu.carroll.EatAndShare.web.service;

import edu.carroll.EatAndShare.backEnd.model.User;
import edu.carroll.EatAndShare.web.form.UserForm;

public interface UserService {
    boolean validateUser(UserForm userForm);
    void saveUser(User user);
    User findByUsername(String username);
}
