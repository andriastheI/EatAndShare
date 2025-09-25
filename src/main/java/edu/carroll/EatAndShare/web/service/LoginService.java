package edu.carroll.EatAndShare.web.service;

import edu.carroll.EatAndShare.backEnd.model.Login;
import edu.carroll.EatAndShare.web.form.LoginForm;

public interface LoginService {
    boolean validateUser(LoginForm loginForm);
    void saveUser(Login login);
}
