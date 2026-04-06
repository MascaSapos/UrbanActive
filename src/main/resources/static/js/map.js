(function () {
  /* ═══════════════════════════════════════════════════════════
     STATE
  ═══════════════════════════════════════════════════════════ */
  var theMap         = null;
  var markersMap     = {};   // id → marker Leaflet
  var markerElems    = {};   // id → .map-marker DOM element
  var activitiesData = {};   // id → activity object (from API)
  var activeId       = null;

  /* ═══════════════════════════════════════════════════════════
     WEATHER PANEL
  ═══════════════════════════════════════════════════════════ */
  function setWeather(a) {
    var actLabel = document.getElementById('w-activity-name');
    if (actLabel) {
      actLabel.textContent = a ? (a.title || a.tipoDeporte || 'Actividad') : 'Selecciona una actividad';
    }

    function setPanel(icon, clima, temp, lluvia, aire) {
      var el = document.getElementById('w-clima-icon');  if (el) el.textContent = icon;
      el = document.getElementById('w-clima-val');       if (el) el.textContent = clima;
      el = document.getElementById('w-temp-val');        if (el) el.textContent = temp;
      el = document.getElementById('w-lluvia-val');      if (el) el.textContent = lluvia;
      el = document.getElementById('w-aire-val');        if (el) el.textContent = aire;
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
    var card = document.getElementById('act-' + id);
    if (!card) return;
    
    var plazas = parseInt(card.getAttribute('data-plazas')) || 0;
    var libres = parseInt(card.getAttribute('data-libres')) || 0;
    var ocupadas = Math.max(0, plazas - libres);
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
    // Habilitar botón de reservar
    var input = document.getElementById('input-actividad-id');
    if (input) input.value = id;
    var btn = document.getElementById('btn-reservar');
    if (btn) btn.disabled = false;
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
    var icon  = a.icon || '📍';
    var cls   = 'map-marker';
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
    theMap = L.map('map', {
      zoomControl: false,
      attributionControl: true
    }).setView([40.420, -3.700], 14);

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
          '<div class="map-popup__desc">'  + (a.desc  || '') + '</div>' +
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

  function initFilterDropdowns() {
    var configs = [
      { btnId: 'btn-deporte', ddId: 'dd-deporte', key: 'deporte', label: 'Deporte' },
      { btnId: 'btn-fecha',   ddId: 'dd-fecha',   key: 'fecha',   label: 'Fecha'   },
      { btnId: 'btn-hora',    ddId: 'dd-hora',    key: 'hora',    label: 'Hora'    },
      { btnId: 'filter-otros', ddId: 'otros-dropdown', key: null,  label: 'Otros'  }
    ];

    configs.forEach(function (cfg) {
      var btn = document.getElementById(cfg.btnId);
      var dd  = document.getElementById(cfg.ddId);
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
            btn.classList.toggle('pill--idle',   !val);

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
    });

    // Search input
    var searchInput = document.getElementById('main-search');
    if (searchInput) {
      searchInput.addEventListener('input', applyFrontendFilters);
    }
  }

  /* ═══════════════════════════════════════════════════════════
     FRONTEND FILTERING
  ═══════════════════════════════════════════════════════════ */
  function applyFrontendFilters() {
    var searchEl = document.getElementById('main-search');
    var text    = searchEl ? (searchEl.value || '').toLowerCase() : '';
    var deporte = (activeFilters.deporte || '').toLowerCase();
    var fecha   = activeFilters.fecha || '';
    var hora    = activeFilters.hora  || '';

    var now   = new Date();
    var today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

    var count = 0;
    document.querySelectorAll('.activity-item').forEach(function (item) {
      var id      = item.getAttribute('data-id') || '';
      var sport   = (item.getAttribute('data-deporte') || '').toLowerCase();
      var content = item.innerText.toLowerCase();

      var matchText    = !text    || content.includes(text);
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
          if (hora === 'manana') { matchHora = h >= 6  && h < 12; }
          else if (hora === 'tarde')  { matchHora = h >= 12 && h < 18; }
          else if (hora === 'noche')  { matchHora = h >= 18; }
        } else {
          matchHora = false;
        }
      }

      var visible = matchText && matchDeporte && matchFecha && matchHora;

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
     BOOT
  ═══════════════════════════════════════════════════════════ */
  document.addEventListener('DOMContentLoaded', function () {
    // Init filter dropdowns FIRST (no dependency on map)
    initFilterDropdowns();

    // Then load map + markers
    fetchActivities()
      .then(function (activities) {
        initMap(activities);
      })
      .catch(function (err) {
        console.warn('No se pudieron cargar actividades del servidor:', err.message);
        initMap([]);
      });
  });

  /* ═══════════════════════════════════════════════════════════
     AJAX RESERVATION
  ═══════════════════════════════════════════════════════════ */
  window.submitReservaAjax = function() {
    var actividadId = document.getElementById('input-actividad-id').value;
    if (!actividadId) return;

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
    .then(function(res) {
      if (res.status === 401 || res.status === 403 || res.redirected) {
        window.location.href = res.url || '/login';
        throw new Error('No autorizado');
      }
      return res.json();
    })
    .then(function(data) {
      if (data.exito) {
        showModal('✅', '¡Reserva exitosa!', data.mensaje);
        
        // Update occupancy locally
        var card = document.getElementById('act-' + actividadId);
        if (card) {
          var libres = parseInt(card.getAttribute('data-libres')) || 0;
          if (libres > 0) {
            card.setAttribute('data-libres', libres - 1);
          }
        }
        setAforo(actividadId);
      } else {
        var msg = data.mensaje || data.message || data.error || (typeof data === 'string' ? data : "Error desconocido interno del servidor");
        showModal('❌', 'Error al reservar', msg);
      }
      btn.innerHTML = originalText;
      btn.disabled = false;
    })
    .catch(function(err) {
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
    setTimeout(function() { modal.classList.add('is-visible'); }, 10);
  }

  window.closeModal = function() {
    var modal = document.getElementById('reserva-modal');
    modal.classList.remove('is-visible');
    setTimeout(function() { modal.style.display = 'none'; }, 300);
  };

}());
