(function () {

  function setWeather(a) {
    var actLabel = document.getElementById('w-activity-name');
    if (actLabel) actLabel.textContent = a.title || '';

    var w = a.weather;
    if (!w) {
      document.getElementById('w-clima-icon').textContent = '—';
      document.getElementById('w-clima-val').textContent = 'Sin datos';
      document.getElementById('w-temp-val').textContent = '—';
      document.getElementById('w-lluvia-val').textContent = '—';
      document.getElementById('w-aire-val').textContent = '—';
      return;
    }
    document.getElementById('w-clima-icon').textContent  = w.climaIcon;
    document.getElementById('w-clima-val').textContent   = w.clima;
    document.getElementById('w-temp-val').textContent    = w.temp;
    document.getElementById('w-lluvia-val').textContent  = w.lluvia;
    document.getElementById('w-aire-val').textContent    = w.aire;
  }

  function getMarkerHtml(a) {
    var mark = a.icon || '📍';
    return '<div class="map-marker">' + mark + '</div>';
  }

  function fetchActivities() {
    return fetch('/api/actividades/map')
      .then(function (response) {
        if (!response.ok) throw new Error('HTTP ' + response.status);
        return response.json();
      })
      .then(function (data) {
        return Array.isArray(data) ? data : [];
      });
  }

  function initMap(activities) {
    var map = L.map('map', {
      zoomControl: false,
      attributionControl: true
    }).setView([40.420, -3.700], 14);

    L.control.zoom({ position: 'topright' }).addTo(map);

    L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> © <a href="https://carto.com/">CARTO</a>',
      subdomains: 'abcd',
      maxZoom: 19
    }).addTo(map);

    activities.forEach(function (a) {
      var icon = L.divIcon({
        className: '',
        html: getMarkerHtml(a),
        iconSize: [42, 42],
        iconAnchor: [21, 21],
        popupAnchor: [0, -26]
      });

      var marker = L.marker([a.lat, a.lng], { icon: icon, title: a.title }).addTo(map);

      marker.bindPopup(
        '<div class="map-popup">' +
          '<div class="map-popup__badge">' + (a.badge || 'Actividad') + '</div>' +
          '<div class="map-popup__title">' + (a.title || '') + '</div>' +
          '<div class="map-popup__desc">' + (a.desc || '') + '</div>' +
        '</div>',
        { closeButton: false }
      );

      marker.on('click', function () { setWeather(a); });
    });

    if (activities.length > 0) {
      setWeather(activities[0]);
    }
  }

  function initFilters() {
    document.querySelectorAll('[data-filter]').forEach(function (btn) {
      btn.addEventListener('click', function () {
        var active = btn.classList.contains('pill--active');
        btn.classList.toggle('pill--active', !active);
        btn.classList.toggle('pill--idle', active);
      });
    });

    var otrosBtn = document.getElementById('filter-otros');
    var otrosDD  = document.getElementById('otros-dropdown');
    if (otrosBtn && otrosDD) {
      otrosBtn.addEventListener('click', function (e) {
        e.stopPropagation();
        var open = otrosDD.classList.toggle('is-open');
        otrosBtn.setAttribute('aria-expanded', open);
      });
      document.addEventListener('click', function () {
        otrosDD.classList.remove('is-open');
        otrosBtn.setAttribute('aria-expanded', 'false');
      });
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    fetchActivities()
      .then(function (activities) {
        initMap(activities);
      })
      .catch(function (err) {
        console.error(err);
        initMap([]);
      });
    initFilters();
  });

}());
