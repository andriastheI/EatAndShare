/**
 * EatAndShare Dynamic Ingredient Form and Recipe Submission Script
 *
 * <p>This script enables users to dynamically add ingredient fields
 * to the recipe submission form and handles the async submission
 * of the recipe data (including images) using the Fetch API.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Dynamically adds new ingredient input fields on user request.</li>
 *   <li>Validates and submits the form asynchronously without page reload.</li>
 *   <li>Handles server responses and displays errors gracefully.</li>
 * </ul>
 *
 * @author Selin
 * @version 1.0
 * @since 2025-10-11
 */

/* -----------------------------------------------------------
   🧂 Add Ingredient Fields Dynamically
----------------------------------------------------------- */

/**
 * Adds a new ingredient input block to the recipe form.
 *
 * <p>Each ingredient entry includes:
 * <ul>
 *   <li>Ingredient name</li>
 *   <li>Quantity</li>
 *   <li>Measurement unit</li>
 * </ul>
 * </p>
 *
 * <p>This allows users to add as many ingredients as they need
 * without reloading or duplicating the form manually.</p>
 */
function addIngredient() {
    const container = document.getElementById('ingredientsList');
    const newItem = document.createElement('div');
    newItem.classList.add('ingredient-item');

    // Create HTML structure for new ingredient
    newItem.innerHTML = `
    <input type="text" name="ingredientName[]" placeholder="Ingredient" required />
    <input type="text" name="quantity[]" placeholder="Quantity" required />
    <input type="text" name="unit[]" placeholder="Unit" required />
  `;

    // Append to form
    container.appendChild(newItem);
}

/* -----------------------------------------------------------
   🍳 Recipe Form Submission (AJAX via Fetch API)
----------------------------------------------------------- */

/**
 * Handles recipe form submission asynchronously.
 *
 * <p>Prevents the default page reload, gathers all form data
 * (including file uploads) into a {@link FormData} object, and
 * sends it to the backend endpoint <strong>/api/recipes</strong>.</p>
 *
 * <p>If submission fails, an error message is displayed on-screen;
 * if successful, the form is reset and a success alert is shown.</p>
 */
document.getElementById("recipeForm").addEventListener("submit", async function(e) {
    e.preventDefault(); // Prevent full page reload on form submit

    const errorDiv = document.getElementById("errorMsg");
    errorDiv.textContent = ""; // Clear previous error messages

    const formData = new FormData(this); // Collect all form fields, including images

    try {
        const response = await fetch('/api/recipes', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error("Failed to submit recipe.");
        }

        alert("✅ Recipe submitted successfully!");
        this.reset(); // Clear the form
    } catch (err) {
        errorDiv.textContent = `❌ ${err.message}`;
    }
}); // ✅ closes the async handler and addEventListener
