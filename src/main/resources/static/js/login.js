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

  /* ── REGISTER ROLE TOGGLE ────────────────────────────── */

  var regRoleButtons = document.querySelectorAll('.reg-role-toggle__btn');
  var regInputRol    = document.getElementById('reg-input-rol');
  var regHintEl      = document.getElementById('reg-role-hint');

  regRoleButtons.forEach(function (btn) {
    btn.addEventListener('click', function () {
      regRoleButtons.forEach(function (b) {
        b.classList.remove('is-active');
        b.setAttribute('aria-pressed', 'false');
      });
      btn.classList.add('is-active');
      btn.setAttribute('aria-pressed', 'true');
      if (regInputRol) regInputRol.value = btn.dataset.role;
      if (regHintEl) regHintEl.textContent = hints[btn.dataset.role] || '';
    });
  });

  /* ── REGISTER FORM VALIDATION ────────────────────────── */

  var regNameEl     = document.getElementById('reg-name');
  var regEmailEl    = document.getElementById('reg-email');
  var regPassEl     = document.getElementById('reg-password');
  var regPass2El    = document.getElementById('reg-password2');
  var regSubmitEl   = document.getElementById('reg-submit-btn');
  var pwBars        = document.querySelectorAll('.pw-strength__bar');
  var pwLabel       = document.getElementById('pw-strength-label');
  var pass2ErrEl    = document.getElementById('reg-pass2-error');

  var pwLevels = ['', 'Débil', 'Regular', 'Fuerte'];

  function getPasswordStrength(pw) {
    if (!pw) return 0;
    var score = 0;
    if (pw.length >= 8) score++;
    if (/[A-Z]/.test(pw) && /[a-z]/.test(pw)) score++;
    if (/\d/.test(pw) && /[^A-Za-z0-9]/.test(pw)) score++;
    return score;
  }

  function updateStrengthBars(score) {
    if (!pwBars.length) return;
    var classes = ['', 'active-weak', 'active-fair', 'active-strong'];
    pwBars.forEach(function (bar, i) {
      bar.className = 'pw-strength__bar';
      if (i < score) bar.classList.add(classes[score]);
    });
    if (pwLabel) pwLabel.textContent = score ? 'Seguridad: ' + pwLevels[score] : '';
  }

  function checkRegisterForm() {
    if (!regSubmitEl) return;
    var nameOk  = regNameEl  && regNameEl.value.trim().length >= 2;
    var emailOk = regEmailEl && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(regEmailEl.value.trim());
    var passOk  = regPassEl  && regPassEl.value.length >= 6;
    var pass2Ok = regPass2El && regPassEl && regPass2El.value === regPassEl.value && regPass2El.value.length > 0;

    if (pass2ErrEl) {
      if (regPass2El && regPass2El.value && !pass2Ok) {
        pass2ErrEl.textContent = 'Las contraseñas no coinciden';
      } else {
        pass2ErrEl.textContent = '';
      }
    }

    regSubmitEl.disabled = !(nameOk && emailOk && passOk && pass2Ok);
  }

  if (regPassEl) {
    regPassEl.addEventListener('input', function () {
      updateStrengthBars(getPasswordStrength(regPassEl.value));
      checkRegisterForm();
    });
  }

  if (regNameEl)  regNameEl.addEventListener('input', checkRegisterForm);
  if (regEmailEl) regEmailEl.addEventListener('input', checkRegisterForm);
  if (regPass2El) regPass2El.addEventListener('input', checkRegisterForm);

  /* ── REGISTER SUBMIT (front-only stub) ───────────────── */

  var regForm = document.getElementById('register-form');
  if (regForm) {
    regForm.addEventListener('submit', function (e) {
      e.preventDefault();
      // TODO: connect to backend
      console.log('Registro:', {
        nombre: regNameEl ? regNameEl.value : '',
        email:  regEmailEl ? regEmailEl.value : '',
        rol:    regInputRol ? regInputRol.value : ''
      });
      showPanel(loginPanel, registerPanel);
      if (hintEl) hintEl.textContent = '¡Cuenta creada! Ya puedes iniciar sesión.';
    });
  }

}());
