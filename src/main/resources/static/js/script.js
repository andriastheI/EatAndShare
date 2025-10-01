const wrapper = document.querySelector('.wrapper');
const loginlink = document.querySelector('.login-link');
const registerlink = document.querySelector('.register-link');
const btnPopup = document.querySelector('.btnLogin-popup');
const iconClose = document.querySelector('.icon-close');

registerlink.addEventListener('click', ()=> {
    wrapper.classList.add('active');

});

loginlink.addEventListener('click', ()=> {
    wrapper.classList.remove('active');

});


btnPopup.addEventListener('click', ()=> {
    wrapper.classList.add('active-popup');

});


iconClose.addEventListener('click', ()=> {
    wrapper.classList.remove('active-popup');

});

// Password validation
const passwordInput = document.getElementById('password');
const confirmPasswordInput = document.getElementById('confirm_password');
const messageElement = document.getElementById('match_message');
const registerButton = document.querySelector('.form-box.register button[type="submit"]');

// Debug: check if elements exist
console.log("Password input:", passwordInput);
console.log("Confirm input:", confirmPasswordInput);
console.log("Message element:", messageElement);

if (passwordInput && confirmPasswordInput && messageElement && registerButton) {
  const checkPasswords = () => {
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    if (password === "" && confirmPassword === "") {
      messageElement.textContent = "";
      registerButton.disabled = true;
      return;
    }

    if (password.length < 8) {
      messageElement.textContent = "Password can't be less than 8 characters";
      messageElement.className = "no-match";
      registerButton.disabled = true;
      return;
    }

    if (password.includes(" ") || confirmPassword.includes(" ")) {
      messageElement.textContent = "No spaces allowed!!";
      messageElement.className = "no-match";
      registerButton.disabled = true;
      return;
    }

    if (password === confirmPassword) {
      messageElement.textContent = "✅ Passwords match";
      messageElement.className = "match";
      registerButton.disabled = false;
    } else {
      messageElement.textContent = "❌ Passwords do not match";
      messageElement.className = "no-match";
      registerButton.disabled = true;
    }
  };

  // Listen to input events instead of just keyup
  passwordInput.addEventListener('input', checkPasswords);
  confirmPasswordInput.addEventListener('input', checkPasswords);
}