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

function addIngredient() {
  const container = document.getElementById("ingredientsList");
  const input = document.createElement("input");
  input.type = "text";
  input.name = "ingredient[]";
  input.placeholder = "e.g. 1 tbsp olive oil";
  input.required = true;
  container.appendChild(input);
}

document.getElementById("recipeForm").addEventListener("submit", async function(e) {
  e.preventDefault();
  const errorDiv = document.getElementById("errorMsg");
  errorDiv.textContent = "";

  const formData = new FormData(this);

  try {
    const response = await fetch('/api/recipes', {
      method: 'POST',
      body: formData
    });

    if (!response.ok) {
      throw new Error("Failed to submit recipe.");
    }

    alert("Recipe submitted successfully!");
    this.reset();
  } catch (err) {
    errorDiv.textContent = err.message;
  }
});
