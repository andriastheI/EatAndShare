/**
 * Filename: UserForm.java
 * Author: Andrias Zelele
 * Date: October 20, 2025
 *
 * Description:
 * This class represents a simple data transfer object (DTO) used to capture
 * user login form input within the EatAndShare web application. It holds
 * the username and password fields entered by the user during login or
 * authentication. This object is typically passed from the controller layer
 * to the service layer for validation and authentication.
 */

package edu.carroll.eatAndShare.backEnd.form;

/**
 * Represents a form containing login credentials entered by the user.
 */
public class UserForm {

    /** The username entered by the user. */
    private String username;

    /** The password entered by the user. */
    private String password;

    /**
     * Default no-argument constructor required by frameworks such as Spring MVC.
     */
    public UserForm() {}

    /**
     * Parameterized constructor for convenience,
     * used primarily in unit testing and manual form binding scenarios.
     *
     * @param username the username input
     * @param password the password input
     */
    public UserForm(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Retrieves the username entered in the form.
     *
     * @return the username as a String
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username for this form.
     *
     * @param username the username entered by the user
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieves the password entered in the form.
     *
     * @return the password as a String
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for this form.
     *
     * @param password the password entered by the user
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
