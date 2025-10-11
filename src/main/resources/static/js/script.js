/**
 * EatAndShare Login/Register Popup and Password Validation Script
 *
 * <p>This script manages interactive behavior for the authentication popup window
 * (login and register forms) and performs client-side password validation before
 * form submission.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Toggle between login and registration forms.</li>
 *   <li>Show/hide popup window dynamically.</li>
 *   <li>Validate password strength and matching in real time.</li>
 *   <li>Disable the register button until valid input is provided.</li>
 * </ul>
 *
 * @author Andrias
 * @version 1.0
 * @since 2025-10-11
 */

// DOM Elements
const wrapper = document.querySelector('.wrapper');
const loginlink = document.querySelector('.login-link');
const registerlink = document.querySelector('.register-link');
const btnPopup = document.querySelector('.btnLogin-popup');
const iconClose = document.querySelector('.icon-close');

/* -----------------------
   🔄 Popup Toggle Handlers
------------------------ */

/** Switches from login form to registration form. */
registerlink.addEventListener('click', () => {
    wrapper.classList.add('active');
});

/** Switches from registration form back to login form. */
loginlink.addEventListener('click', () => {
    wrapper.classList.remove('active');
});

/** Opens the login/register popup modal. */
btnPopup.addEventListener('click', () => {
    wrapper.classList.add('active-popup');
});

/** Closes the popup modal window. */
iconClose.addEventListener('click', () => {
    wrapper.classList.remove('active-popup');
});

/* -----------------------
   🔐 Password Validation
------------------------ */

// DOM elements for password fields and feedback message
const passwordInput = document.getElementById('password');
const confirmPasswordInput = document.getElementById('confirm_password');
const messageElement = document.getElementById('match_message');
const registerButton = document.querySelector('.form-box.register button[type="submit"]');

// Debugging logs (optional for development)
console.log("Password input:", passwordInput);
console.log("Confirm input:", confirmPasswordInput);
console.log("Message element:", messageElement);

/**
 * Validates password fields in real time.
 * Ensures passwords meet length and spacing rules,
 * and match before enabling the submit button.
 */
if (passwordInput && confirmPasswordInput && messageElement && registerButton) {
    const checkPasswords = () => {
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        // No input yet
        if (password === "" && confirmPassword === "") {
            messageElement.textContent = "";
            registerButton.disabled = true;
            return;
        }

        // Too short
        if (password.length < 8) {
            messageElement.textContent = "Password can't be less than 8 characters";
            messageElement.className = "no-match";
            registerButton.disabled = true;
            return;
        }

        // No spaces allowed
        if (password.includes(" ") || confirmPassword.includes(" ")) {
            messageElement.textContent = "No spaces allowed!!";
            messageElement.className = "no-match";
            registerButton.disabled = true;
            return;
        }

        // ✅ Valid and matching passwords
        if (password === confirmPassword) {
            messageElement.textContent = "✅ Passwords match";
            messageElement.className = "match";
            registerButton.disabled = false;
        } else {
            // ❌ Mismatch
            messageElement.textContent = "❌ Passwords do not match";
            messageElement.className = "no-match";
            registerButton.disabled = true;
        }
    };

    // Listen for real-time changes
    passwordInput.addEventListener('input', checkPasswords);
    confirmPasswordInput.addEventListener('input', checkPasswords);
}
