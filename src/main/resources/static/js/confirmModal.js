<!-- File: src/main/resources/static/js/confirm-modal.js                    -->
<!-- ===================================================================== -->
(() => {
  // Why: centralize modal logic and ensure accessibility
  let pendingForm = null;
  let lastFocused = null;

  const modal = document.getElementById('confirmModal');
  const titleEl = document.getElementById('confirmRecipeTitle');
  const btnCancel = modal.querySelector('[data-action="cancel"]');
  const btnConfirm = modal.querySelector('[data-action="confirm"]');
  const backdrop = modal.querySelector('.modal__backdrop');

  function openModal(recipeTitle, form) {
    pendingForm = form;
    lastFocused = document.activeElement;
    titleEl.textContent = `"${recipeTitle || 'this recipe'}"`;
    modal.setAttribute('aria-hidden', 'false');
    document.body.classList.add('modal-open');
    // focus first action
    btnCancel.focus();
    trapFocus(true);
  }

  function closeModal() {
    trapFocus(false);
    modal.setAttribute('aria-hidden', 'true');
    document.body.classList.remove('modal-open');
    if (lastFocused) lastFocused.focus();
    pendingForm = null;
  }

  function trapFocus(enable) {
    function handler(e) {
      if (e.key !== 'Tab') return;
      const focusables = modal.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
      if (!focusables.length) return;
      const first = focusables[0], last = focusables[focusables.length - 1];
      if (e.shiftKey && document.activeElement === first) { e.preventDefault(); last.focus(); }
      else if (!e.shiftKey && document.activeElement === last) { e.preventDefault(); first.focus(); }
    }
    if (enable) modal.addEventListener('keydown', handler);
    else modal.removeEventListener('keydown', handler);
  }

  document.addEventListener('click', (e) => {
    // Close on backdrop click
    if (e.target === backdrop || e.target.dataset.action === 'cancel') {
      e.preventDefault();
      closeModal();
    } else if (e.target.dataset.action === 'confirm') {
      // Submit the pending form
      if (pendingForm) pendingForm.submit();
      closeModal();
    }
  });

  document.addEventListener('keydown', (e) => {
    if (modal.getAttribute('aria-hidden') === 'false' && e.key === 'Escape') {
      e.preventDefault();
      closeModal();
    }
  });

  // Intercept delete form submits
  document.addEventListener('submit', (e) => {
    const form = e.target;
    if (!form.classList.contains('js-delete-form')) return;
    e.preventDefault(); // pause submit
    const title = form.getAttribute('data-title') || 'this recipe';
    openModal(title, form);
  });
})();