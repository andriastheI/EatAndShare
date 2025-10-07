function addIngredient() {
    const container = document.getElementById('ingredientsList');
    const newItem = document.createElement('div');
    newItem.classList.add('ingredient-item');
    newItem.innerHTML = `
        <input type="text" name="ingredientName[]" placeholder="Ingredient" required />
        <input type="text" name="quantity[]" placeholder="Quantity" required />
        <input type="text" name="unit[]" placeholder="Unit" required />
    `;
    container.appendChild(newItem);
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