(function () {

  /* ── ROLE TOGGLE ─────────────────────────────────────── */

  var roleButtons = document.querySelectorAll('.role-toggle__btn');
  var inputRol    = document.getElementById('input-rol');
  var hintEl      = document.getElementById('role-hint');

  var hints = {
    deportista: 'Buscas y reservas actividades deportivas',
    organizador: 'Publicas y gestionas tus propias sesiones'
  };

  roleButtons.forEach(function (btn) {
    btn.addEventListener('click', function () {
      roleButtons.forEach(function (b) {
        b.classList.remove('is-active');
        b.setAttribute('aria-pressed', 'false');
      });
      btn.classList.add('is-active');
      btn.setAttribute('aria-pressed', 'true');
      if (inputRol) inputRol.value = btn.dataset.role;
      if (hintEl) hintEl.textContent = hints[btn.dataset.role] || '';
    });
  });

  /* ── LOGIN FORM VALIDATION ───────────────────────────── */

  var emailEl  = document.getElementById('email');
  var passEl   = document.getElementById('password');
  var submitEl = document.getElementById('submit-btn');

  function checkLoginForm() {
    if (!submitEl) return;
    submitEl.disabled = !(emailEl.value.trim() && passEl.value.length >= 6);
  }

  if (emailEl) emailEl.addEventListener('input', checkLoginForm);
  if (passEl)  passEl.addEventListener('input', checkLoginForm);

  /* ── PANEL SWITCHING (Login ↔ Register) ──────────────── */

  var loginPanel    = document.getElementById('panel-login');
  var registerPanel = document.getElementById('panel-register');
  var goRegisterBtn = document.getElementById('go-register');
  var goLoginBtn    = document.getElementById('go-login');

  function showPanel(panelToShow, panelToHide) {
    if (!panelToShow || !panelToHide) return;
    panelToHide.classList.remove('is-visible');
    panelToShow.classList.add('is-visible');
  }

  if (goRegisterBtn) {
    goRegisterBtn.addEventListener('click', function (e) {
      e.preventDefault();
      showPanel(registerPanel, loginPanel);
    });
  }

  if (goLoginBtn) {
    goLoginBtn.addEventListener('click', function (e) {
      e.preventDefault();
      showPanel(loginPanel, registerPanel);
    });
  }

  var goLoginBtn2 = document.getElementById('go-login-2');
  if (goLoginBtn2) {
    goLoginBtn2.addEventListener('click', function (e) {
      e.preventDefault();
      showPanel(loginPanel, registerPanel);
    });
  }

}()

