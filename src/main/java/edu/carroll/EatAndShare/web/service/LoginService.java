package edu.carroll.EatAndShare.web.service;

import edu.carroll.EatAndShare.web.form.LoginForm;
import edu.carroll.EatAndShare.web.form.RegisterForm;

public interface LoginService {
    boolean validateUser(LoginForm loginForm);
    boolean validateUser(RegisterForm registerForm);
}
