(function () {
  /* ═══════════════════════════════════════════════════════════
     STATE
  ═══════════════════════════════════════════════════════════ */
  var theMap = null;
  var markersMap = {};   // id → marker Leaflet
  var markerElems = {};   // id → .map-marker DOM element
  var activitiesData = {};   // id → activity object (from API)
  var activeId = null;
  var currentNotifications = [];
  var reservedActivities = {};

  /* ═══════════════════════════════════════════════════════════
     WEATHER PANEL
  ═══════════════════════════════════════════════════════════ */
  function setWeather(a) {
    var actLabel = document.getElementById('w-activity-name');
    if (actLabel) {
      actLabel.textContent = a ? (a.title || a.tipoDeporte || 'Actividad') : 'Selecciona una actividad';
    }

    function setPanel(icon, clima, temp, lluvia, aire) {
      var el = document.getElementById('w-clima-icon'); if (el) el.textContent = icon;
      el = document.getElementById('w-clima-val'); if (el) el.textContent = clima;
      el = document.getElementById('w-temp-val'); if (el) el.textContent = temp;
      el = document.getElementById('w-lluvia-val'); if (el) el.textContent = lluvia;
      el = document.getElementById('w-aire-val'); if (el) el.textContent = aire;
    }

    var w = a && a.weather;
    if (w) {
      setPanel(
        w.climaIcon || '☀️',
        w.clima || '—',
        w.temp || '—',
        w.lluvia || '—',
        w.aire || '—'
      );
      return;
    }

    if (!a) {
      setPanel('—', 'Sin datos', '—', '—', '—');
      return;
    }

    /* Actividad sin fila InformeMeteorologico: ejemplo visual */
    setPanel('☀️', 'Soleado', '22°C', '10%', 'Buena');
  }

  /* ═══════════════════════════════════════════════════════════
     AFORO PANEL
  ═══════════════════════════════════════════════════════════ */
  function setAforo(id) {
    if (!id) {
      if (document.getElementById('w-aforo-libres')) {
        document.getElementById('w-aforo-libres').textContent = '-- libres';
        document.getElementById('w-aforo-total').textContent = '-- plazas';
        document.getElementById('w-aforo-fill').style.width = '0%';
        document.getElementById('w-aforo-marker').style.left = '0%';
      }
      return;
    }

    var activity = activitiesData[id];
    if (!activity) return;

    var plazas = typeof activity.plazasTotal === 'number' ? activity.plazasTotal : 0;
    var ocupadas = typeof activity.plazasOcupadas === 'number' ? activity.plazasOcupadas : 0;
    var libres = Math.max(0, plazas - ocupadas);
    var pct = plazas > 0 ? (ocupadas / plazas) * 100 : 0;

    var libresEl = document.getElementById('w-aforo-libres');
    if (libresEl) {
      libresEl.textContent = libres + ' libres';
      document.getElementById('w-aforo-total').textContent = plazas + ' plazas';
      document.getElementById('w-aforo-fill').style.width = pct + '%';
      document.getElementById('w-aforo-marker').style.left = pct + '%';
    }
  }

  /* ═══════════════════════════════════════════════════════════
     MARKER HIGHLIGHTING
  ═══════════════════════════════════════════════════════════ */
  function setActiveMarker(id) {
    if (activeId && markerElems[activeId]) {
      markerElems[activeId].classList.remove('is-active');
    }
    activeId = id;
    if (id && markerElems[id]) {
      markerElems[id].classList.add('is-active');
    }
  }

  /* ═══════════════════════════════════════════════════════════
     BUTTON STYLES
  ═══════════════════════════════════════════════════════════ */
  function setBtnReservado(btn) {
    btn.disabled = false; // Keep clickable so user sees "already registered" message
    btn.textContent = 'Plaza reservada';
    btn.style.background = '#ccc';
    btn.style.color = '#333';
    btn.style.cursor = 'not-allowed';
  }

  function setBtnCompleto(btn) {
    btn.disabled = true;
    btn.textContent = 'Aforo completo';
    btn.style.background = '#ccc';
    btn.style.color = '#333';
    btn.style.cursor = 'not-allowed';
  }

  function setBtnNormal(btn) {
    btn.disabled = false;
    btn.textContent = 'Reservar plaza';
    btn.style.background = '';
    btn.style.color = '';
    btn.style.cursor = 'pointer';
  }

  /* ═══════════════════════════════════════════════════════════
     CARD HIGHLIGHTING
  ═══════════════════════════════════════════════════════════ */
  function highlightCard(id) {
    document.querySelectorAll('.activity-item').forEach(function (c) {
      c.classList.remove('is-selected');
    });
    if (!id) return;
    var card = document.getElementById('act-' + id);
    if (card) {
      card.classList.add('is-selected');
      card.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
    // Habilitar botón de reservar y actualizar barra de aforo
    var input = document.getElementById('input-actividad-id');
    if (input) input.value = id;

    var btnDetalles = document.getElementById('btn-detalles');
    if (btnDetalles) {
      btnDetalles.onclick = function () {
        window.location.href = '/actividades/' + id;
      };
    }

    var btn = document.getElementById('btn-reservar');
    var activity = activitiesData[id];

    if (activity && btn) {
      if (activity.plazasOcupadas >= activity.plazasTotal || activity.estado === 'CERRADA/COMPLETA') {
        setBtnCompleto(btn);
      } else if (reservedActivities[id]) {
        setBtnReservado(btn);
      } else {
        setBtnNormal(btn);
        // Check inscription status from server
        fetch('/api/actividades/' + id + '/check-inscripcion')
          .then(function (res) { return res.ok ? res.json() : null; })
          .then(function (data) {
            if (data && data.inscrito) {
              reservedActivities[id] = true;
              // Only update if this activity is still the selected one
              if (document.getElementById('input-actividad-id').value === String(id)) {
                setBtnReservado(btn);
              }
            }
          })
          .catch(function () { /* ignore */ });
      }
    } else if (btn) {
      setBtnNormal(btn);
    }
  }

  /* ═══════════════════════════════════════════════════════════
     UNIFIED SELECT: called from map click OR card click
  ═══════════════════════════════════════════════════════════ */
  function selectActivity(id, source) {
    if (!id) return;
    setActiveMarker(id);
    highlightCard(id);
    setWeather(activitiesData[id] || null);
    setAforo(id);

    if (source !== 'map' && theMap && markersMap[id]) {
      theMap.flyTo(markersMap[id].getLatLng(), 15, { animate: true, duration: 0.4 });
      setTimeout(function () { markersMap[id].openPopup(); }, 450);
    }
  }

  /* ═══════════════════════════════════════════════════════════
     PUBLIC APIs used by inline script
  ═══════════════════════════════════════════════════════════ */
  // Called when user clicks a card in the list
  window.seleccionarActividad = function (el) {
    var id = el ? el.getAttribute('data-id') : null;
    if (!id) return;
    selectActivity(id, 'card');
  };

  // Called from map.js marker click (legacy bridge)
  window.selectActivityCard = function (id) {
    highlightCard(id);
    setWeather(activitiesData[id] || null);
    setAforo(id);
  };

  // Called from map.js to fly map to marker
  window.focusMapMarker = function (id) {
    var marker = markersMap[id];
    if (marker && theMap) {
      theMap.flyTo(marker.getLatLng(), 15, { animate: true, duration: 0.4 });
      setTimeout(function () { marker.openPopup(); }, 450);
      setActiveMarker(id);
    }
  };

  /* ═══════════════════════════════════════════════════════════
     MARKER HTML
  ═══════════════════════════════════════════════════════════ */
  function getMarkerHtml(a) {
    var sport = (a.tipoDeporte || a.badge || '').toLowerCase();
    var icon = a.icon || '📍';
    var cls = 'map-marker';
    if (sport) cls += ' map-marker--' + sport;
    return '<div class="' + cls + '" data-marker-id="' + (a.id || '') + '">' + icon + '</div>';
  }

  /* ═══════════════════════════════════════════════════════════
     FETCH ACTIVITIES
  ═══════════════════════════════════════════════════════════ */
  function fetchActivities() {
    return fetch('/api/actividades/map')
      .then(function (res) {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.json();
      })
      .then(function (data) {
        return Array.isArray(data) ? data : [];
      });
  }

  /* ═══════════════════════════════════════════════════════════
     INIT LEAFLET MAP
  ═══════════════════════════════════════════════════════════ */
  function initMap(activities) {
    var savedCenter = localStorage.getItem('ua_map_center');
    var savedZoom   = localStorage.getItem('ua_map_zoom');
    
    var initialCenter = savedCenter ? JSON.parse(savedCenter) : [40.420, -3.700];
    var initialZoom   = savedZoom ? parseInt(savedZoom, 10) : 14;

    theMap = L.map('map', {
      zoomControl: false,
      attributionControl: true
    }).setView(initialCenter, initialZoom);

    // Guardar estado del mapa cuando cambie
    theMap.on('moveend', function() {
      localStorage.setItem('ua_map_center', JSON.stringify([theMap.getCenter().lat, theMap.getCenter().lng]));
      localStorage.setItem('ua_map_zoom', theMap.getZoom().toString());
    });

    L.control.zoom({ position: 'topright' }).addTo(theMap);

    L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> © <a href="https://carto.com/">CARTO</a>',
      subdomains: 'abcd',
      maxZoom: 19
    }).addTo(theMap);

    activities.forEach(function (a) {
      // Store data for weather panel sync
      if (a.id != null) activitiesData[a.id] = a;

      var icon = L.divIcon({
        className: '',
        html: getMarkerHtml(a),
        iconSize: [46, 46],
        iconAnchor: [23, 23],
        popupAnchor: [0, -28]
      });

      var marker = L.marker([a.lat, a.lng], { icon: icon, title: a.title || '' }).addTo(theMap);

      if (a.id != null) markersMap[a.id] = marker;

      marker.bindPopup(
        '<div class="map-popup">' +
        '<div class="map-popup__badge">' + (a.badge || a.tipoDeporte || 'Actividad') + '</div>' +
        '<div class="map-popup__title">' + (a.title || '') + '</div>' +
        '<div class="map-popup__desc">' + (a.desc || '') + '</div>' +
        '<div class="map-popup__meta" style="font-size:0.75rem; color: #666; margin-top: 4px;">' + (a.tipoEspacio ? '🏠 ' + a.tipoEspacio : '') + '</div>' +
        '</div>',
        { closeButton: false }
      );

      marker.on('add', function () {
        var el = marker.getElement();
        if (el && a.id != null) {
          var inner = el.querySelector('.map-marker');
          if (inner) markerElems[a.id] = inner;
        }
      });

      // Map click → bidirectional sync
      marker.on('click', function () {
        selectActivity(a.id, 'map');
      });
    });

    // Show weather for first activity on load
    if (activities.length > 0 && activities[0].id != null) {
      setWeather(activities[0]);
    }
  }

  /* ═══════════════════════════════════════════════════════════
     FILTER DROPDOWNS
  ═══════════════════════════════════════════════════════════ */
  var activeFilters = { deporte: '', fecha: '', hora: '' };
  var filterPlazasLibres = false;

  function initFilterDropdowns() {
    var configs = [
      { btnId: 'btn-deporte', ddId: 'dd-deporte', key: 'deporte', label: 'Deporte' },
      { btnId: 'btn-fecha', ddId: 'dd-fecha', key: 'fecha', label: 'Fecha' },
      { btnId: 'btn-hora', ddId: 'dd-hora', key: 'hora', label: 'Hora' },
      { btnId: 'filter-otros', ddId: 'otros-dropdown', key: null, label: 'Otros' }
    ];

    configs.forEach(function (cfg) {
      var btn = document.getElementById(cfg.btnId);
      var dd = document.getElementById(cfg.ddId);
      if (!btn || !dd) return;

      // Toggle dropdown on pill click
      btn.addEventListener('click', function (e) {
        e.stopPropagation();
        // Close all other open dropdowns
        document.querySelectorAll('.filter-dropdown.is-open').forEach(function (d) {
          if (d !== dd) {
            d.classList.remove('is-open');
            var prev = d.previousElementSibling;
            if (prev) prev.setAttribute('aria-expanded', 'false');
          }
        });
        var isOpen = dd.classList.toggle('is-open');
        btn.setAttribute('aria-expanded', String(isOpen));
      });

      // Handle option selection
      if (cfg.key) {
        dd.querySelectorAll('.filter-dropdown__item[data-value]').forEach(function (item) {
          item.addEventListener('click', function (e) {
            e.stopPropagation();
            var val = item.getAttribute('data-value');
            activeFilters[cfg.key] = val;

            // Mark selected item visually
            dd.querySelectorAll('.filter-dropdown__item').forEach(function (i) {
              i.classList.remove('is-selected');
            });
            item.classList.add('is-selected');

            // Update pill label (first text node only)
            var labelText = val ? item.textContent.trim() : cfg.label;
            // Safe: replace only the text node, keep the SVG arrow intact
            var textNode = null;
            btn.childNodes.forEach(function (n) {
              if (n.nodeType === Node.TEXT_NODE) textNode = n;
            });
            if (textNode) {
              textNode.textContent = ' ' + labelText + ' ';
            } else {
              btn.firstChild.textContent = labelText;
            }

            btn.classList.toggle('pill--active', !!val);
            btn.classList.toggle('pill--idle', !val);

            dd.classList.remove('is-open');
            btn.setAttribute('aria-expanded', 'false');
            applyFrontendFilters();
          });
        });
      }
    });

    // Close all dropdowns when clicking outside
    document.addEventListener('click', function () {
      document.querySelectorAll('.filter-dropdown.is-open').forEach(function (d) {
        d.classList.remove('is-open');
        var prev = d.previousElementSibling;
        if (prev) prev.setAttribute('aria-expanded', 'false');
      });
      // Cerrar también la campana si está abierta
      var bellDD = document.getElementById('notification-dropdown');
      if (bellDD) bellDD.classList.remove('is-open');
    });

    // Search input
    var searchInput = document.getElementById('main-search');
    if (searchInput) {
      searchInput.addEventListener('input', applyFrontendFilters);
    }

    // Toggle "Plazas disponibles"
    var btnPlazas = document.getElementById('btn-plazas-libres');
    if (btnPlazas) {
      btnPlazas.addEventListener('click', function (e) {
        e.stopPropagation();
        filterPlazasLibres = !filterPlazasLibres;
        btnPlazas.classList.toggle('pill--active', filterPlazasLibres);
        btnPlazas.classList.toggle('pill--idle', !filterPlazasLibres);
        btnPlazas.setAttribute('aria-pressed', String(filterPlazasLibres));
        applyFrontendFilters();
      });
    }
  }

  /* ═══════════════════════════════════════════════════════════
     FRONTEND FILTERING
  ═══════════════════════════════════════════════════════════ */
  function applyFrontendFilters() {
    var searchEl = document.getElementById('main-search');
    var text = searchEl ? (searchEl.value || '').toLowerCase() : '';
    var deporte = (activeFilters.deporte || '').toLowerCase();
    var fecha = activeFilters.fecha || '';
    var hora = activeFilters.hora || '';

    var now = new Date();
    var today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

    var count = 0;
    document.querySelectorAll('.activity-item').forEach(function (item) {
      var id = item.getAttribute('data-id') || '';
      var sport = (item.getAttribute('data-deporte') || '').toLowerCase();
      var content = item.innerText.toLowerCase();

      var matchText = !text || content.includes(text);
      var matchDeporte = !deporte || sport.includes(deporte);

      // ── Filtro Fecha ─────────────────────────────────────────
      var matchFecha = true;
      if (fecha) {
        var rawFecha = item.getAttribute('data-fecha') || '';
        var cardDate = rawFecha ? new Date(rawFecha) : null;
        if (cardDate && !isNaN(cardDate)) {
          var cardDay = new Date(cardDate.getFullYear(), cardDate.getMonth(), cardDate.getDate());
          if (fecha === 'hoy') {
            matchFecha = cardDay.getTime() === today.getTime();
          } else if (fecha === 'manana') {
            var tomorrow = new Date(today); tomorrow.setDate(today.getDate() + 1);
            matchFecha = cardDay.getTime() === tomorrow.getTime();
          } else if (fecha === 'semana') {
            var weekEnd = new Date(today); weekEnd.setDate(today.getDate() + 7);
            matchFecha = cardDay >= today && cardDay <= weekEnd;
          } else if (fecha === 'mes') {
            var monthEnd = new Date(today); monthEnd.setMonth(today.getMonth() + 1);
            matchFecha = cardDay >= today && cardDay <= monthEnd;
          }
        } else {
          matchFecha = false;
        }
      }

      // ── Filtro Hora ──────────────────────────────────────────
      var matchHora = true;
      if (hora) {
        var rawFecha2 = item.getAttribute('data-fecha') || '';
        var cardDate2 = rawFecha2 ? new Date(rawFecha2) : null;
        if (cardDate2 && !isNaN(cardDate2)) {
          var h = cardDate2.getHours();
          if (hora === 'manana') { matchHora = h >= 6 && h < 12; }
          else if (hora === 'tarde') { matchHora = h >= 12 && h < 18; }
          else if (hora === 'noche') { matchHora = h >= 18; }
        } else {
          matchHora = false;
        }
      }

      // ── Filtro Plazas libres ─────────────────────────────────
      var matchPlazas = true;
      if (filterPlazasLibres) {
        var libres = parseInt(item.getAttribute('data-libres'), 10);
        matchPlazas = !isNaN(libres) && libres > 0;
      }

      var visible = matchText && matchDeporte && matchFecha && matchHora && matchPlazas;

      // ── Tarjeta DOM ──────────────────────────────────────────
      item.style.display = visible ? '' : 'none';
      if (visible) count++;

      // ── Marker del mapa ───────────────────────────────────────
      if (id && markersMap[id] && theMap) {
        if (visible) {
          if (!theMap.hasLayer(markersMap[id])) {
            markersMap[id].addTo(theMap);
          }
        } else {
          if (theMap.hasLayer(markersMap[id])) {
            markersMap[id].remove();
          }
        }
      }
    });

    var badge = document.getElementById('act-count');
    if (badge) badge.textContent = count + ' encontradas';
  }

  window.applyFrontendFilters = applyFrontendFilters;

  /* ═══════════════════════════════════════════════════════════
     FULL-CAPACITY CARD STYLING
  ═══════════════════════════════════════════════════════════ */
  function updateFullCards() {
    document.querySelectorAll('.activity-item').forEach(function (item) {
      var libres = parseInt(item.getAttribute('data-libres'), 10);
      item.classList.toggle('is-full', !isNaN(libres) && libres <= 0);
    });
  }

  /* ═══════════════════════════════════════════════════════════
     BOOT
  ═══════════════════════════════════════════════════════════ */
  var CACHE_KEY = 'urbanactive_activities_cache_v3';

  function getCachedActivities() {
    try {
      var raw = localStorage.getItem(CACHE_KEY);
      if (raw) return JSON.parse(raw);
    } catch (e) { /* ignore */ }
    return null;
  }

  function setCachedActivities(data) {
    try {
      localStorage.setItem(CACHE_KEY, JSON.stringify(data));
    } catch (e) { /* ignore quota errors */ }
  }

  document.addEventListener('DOMContentLoaded', function () {
    function checkNotifications() {
      fetch('/api/notificaciones/pendientes?t=' + Date.now())
        .then(function (res) { return res.ok ? res.json() : null; })
        .then(function (data) {
          var dot = document.getElementById('bell-dot');
          if (dot && data !== null) {
            dot.style.display = data.pendiente ? '' : 'none';
            currentNotifications = data.mensajes || [];
          }
        })
        .catch(function () { });
    }

    checkNotifications();

    // ── Lógica de la campana (Desplegable) ──────────────────────────────────
    var bellBtn = document.getElementById('bell-btn');
    var bellDD = document.getElementById('notification-dropdown');
    var bellList = document.getElementById('notification-list');

    if (bellBtn && bellDD && bellList) {
      bellBtn.addEventListener('click', function (e) {
        e.stopPropagation();
        var isOpen = bellDD.classList.toggle('is-open');
        if (isOpen) {
          if (currentNotifications.length === 0) {
            bellList.innerHTML = '<div class="notification-item notification-item--empty">No hay avisos nuevos</div>';
          } else {
            bellList.innerHTML = currentNotifications.map(function (m) {
              return '<div class="notification-item">' + m + '</div>';
            }).join('');
          }
        }
      });
    }

    // Init filter dropdowns FIRST (no dependency on map)
    initFilterDropdowns();

    var cached = getCachedActivities();

    if (cached && cached.length > 0) {
      // Instant init with cached data
      initMap(cached);
      updateFullCards();

      // Then refresh in background
      fetchActivities()
        .then(function (fresh) {
          setCachedActivities(fresh);
          // Update activitiesData with fresh occupancy numbers
          fresh.forEach(function (a) {
            if (a.id != null) activitiesData[a.id] = a;
          });
          // Refresh the aforo panel if an activity is selected
          if (activeId) setAforo(activeId);
        })
        .catch(function () { /* keep using cache */ });
    } else {
      // First visit: fetch then init
      fetchActivities()
        .then(function (activities) {
          setCachedActivities(activities);
          initMap(activities);
          updateFullCards();
        })
        .catch(function (err) {
          console.warn('No se pudieron cargar actividades del servidor:', err.message);
          initMap([]);
        });
    }

    // ── URL PARAM CHECK ──────────────────────────────────────
    var params = new URLSearchParams(window.location.search);
    var targetId = params.get('actividadId');
    if (targetId) {
      setTimeout(function() {
        if (activitiesData[targetId]) {
          selectActivity(targetId, 'url');
        } else {
          // If not in data yet (async fetch), try again in a bit
          setTimeout(function() {
            if (activitiesData[targetId]) selectActivity(targetId, 'url');
          }, 1000);
        }
      }, 600);
    }
  });

  /* ═══════════════════════════════════════════════════════════
     AJAX RESERVATION
  ═══════════════════════════════════════════════════════════ */
  window.submitReservaAjax = function () {
    var actividadId = document.getElementById('input-actividad-id').value;
    if (!actividadId) return;

    var btn = document.getElementById('btn-reservar');
    var originalText = btn.innerHTML;
    btn.innerHTML = 'Verificando...';
    btn.disabled = true;

    fetch('/api/actividades/' + actividadId + '/check-inscripcion')
      .then(function (res) {
        if (!res.ok) throw new Error('Network error');
        return res.json();
      })
      .then(function (data) {
        // Si el usuario ya está inscrito, mostrar mensaje directamente
        if (data && data.inscrito) {
          reservedActivities[actividadId] = true;
          setBtnReservado(btn);
          showModal('❌', 'Ya estás inscrito', 'Ya tienes una plaza reservada en esta actividad.');
          return;
        }

        btn.innerHTML = originalText;
        btn.disabled = false;

        // Check for weather alert before reserving
        var activity = activitiesData[actividadId];
        if (activity && activity.weather && activity.weather.alerta) {
          // Build a descriptive warning message
          var w = activity.weather;
          var details = 'Temperatura: ' + (w.temp || '—') + '  ·  Aire: ' + (w.aire || '—');
          document.getElementById('weather-warn-msg').textContent = details;

          var modal = document.getElementById('weather-warn-modal');
          modal.style.display = 'flex';
          setTimeout(function () { modal.classList.add('is-visible'); }, 10);
          return; // Don't reserve yet, wait for user confirmation
        }

        // No alert → reserve directly
        doReserva(actividadId);
      })
      .catch(function (err) {
        // Fallback silente en caso de error: intenta la reserva normal
        btn.innerHTML = originalText;
        btn.disabled = false;
        doReserva(actividadId);
      });
  };

  function doReserva(actividadId) {
    var btn = document.getElementById('btn-reservar');
    var originalText = btn.innerHTML;
    btn.innerHTML = 'Reservando...';
    btn.disabled = true;

    // Get CSRF token if present
    var form = document.getElementById('form-reservar');
    var csrfToken = form.querySelector('input[name="_csrf"]');
    var bodyParams = new URLSearchParams();
    if (csrfToken) bodyParams.append('_csrf', csrfToken.value);

    fetch('/api/actividades/' + actividadId + '/reservar', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: bodyParams
    })
      .then(function (res) {
        if (res.status === 401 || res.status === 403 || res.redirected) {
          window.location.href = res.url || '/login';
          throw new Error('No autorizado');
        }
        return res.json();
      })
      .then(function (data) {
        if (data.exito) {
          showModal('✅', '¡Reserva exitosa!', data.mensaje);
          reservedActivities[actividadId] = true;

          // Update occupancy locally
          if (activitiesData[actividadId]) {
            activitiesData[actividadId].plazasOcupadas = (activitiesData[actividadId].plazasOcupadas || 0) + 1;
            
            // Update the activity card in the left panel
            var card = document.getElementById('act-' + actividadId);
            if (card) {
              var act = activitiesData[actividadId];
              var libres = Math.max(0, (act.plazasTotal || 0) - (act.plazasOcupadas || 0));
              card.setAttribute('data-libres', libres);
              var plazasEl = card.querySelector('.activity-item__plazas');
              if (plazasEl) plazasEl.textContent = libres + ' libres';
              card.classList.toggle('is-full', libres <= 0);
            }
          }
          setAforo(actividadId);
          setBtnReservado(btn);
        } else {
          var msg = data.mensaje || data.message || data.error || (typeof data === 'string' ? data : "Error desconocido interno del servidor");
          showModal('❌', 'Error al reservar', msg);
          btn.innerHTML = originalText;
          btn.disabled = false;
        }
      })
      .catch(function (err) {
        btn.innerHTML = originalText;
        btn.disabled = false;
        showModal('❌', 'Error de red', err.message || "No se ha podido contactar con el servidor");
        console.error(err);
      });
  };

  function showModal(icon, title, msg) {
    document.getElementById('modal-icon').innerText = icon;
    document.getElementById('modal-title').innerText = title;
    document.getElementById('modal-msg').innerText = msg;

    var modal = document.getElementById('reserva-modal');
    modal.style.display = 'flex';
    setTimeout(function () { modal.classList.add('is-visible'); }, 10);
  }

  window.closeModal = function () {
    var modal = document.getElementById('reserva-modal');
    modal.classList.remove('is-visible');
    setTimeout(function () { modal.style.display = 'none'; }, 300);
  };

  // ── Weather Warning Modals ─────────────────────────────────
  window.closeWeatherWarn = function () {
    var modal = document.getElementById('weather-warn-modal');
    modal.classList.remove('is-visible');
    setTimeout(function () { modal.style.display = 'none'; }, 300);
  };

  window.closeWeatherWarnAndReserve = function () {
    var actividadId = document.getElementById('input-actividad-id').value;
    window.closeWeatherWarn();
    if (actividadId) doReserva(actividadId);
  };

}());
