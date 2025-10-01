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
});   // ✅ closes the function AND the addEventListener