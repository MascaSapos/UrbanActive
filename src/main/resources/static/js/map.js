(function () {

  var activities = [
    {
      id: 'baloncesto',
      lat: 40.4230, lng: -3.7100,
      title: 'Baloncesto 3v3',
      desc: 'Partido amistoso en plena urbana. Faltan 2 jugadores.',
      icon: '🏀',
      badge: 'Deporte',
      weather: { clima: 'Soleado', climaIcon: '☀️', temp: '21°C', lluvia: '5%', aire: 'Buena' }
    },
    {
      id: 'running',
      lat: 40.4170, lng: -3.7000,
      title: 'Ruta Running',
      desc: 'Grupo principiante. Recorrido de 5 km por el parque.',
      icon: '🏃',
      badge: 'Running',
      weather: { clima: 'Soleado', climaIcon: '☀️', temp: '22°C', lluvia: '10%', aire: 'Buena' }
    },
    {
      id: 'ciclismo',
      lat: 40.4260, lng: -3.6900,
      title: 'Ciclismo Urbano',
      desc: 'Salida por carriles bici, nivel medio.',
      icon: '🚴',
      badge: 'Ciclismo',
      weather: { clima: 'Nublado', climaIcon: '⛅', temp: '19°C', lluvia: '20%', aire: 'Moderada' }
    }
  ];

  function setWeather(a) {
    var w = a.weather;
    document.getElementById('w-clima-icon').textContent  = w.climaIcon;
    document.getElementById('w-clima-val').textContent   = w.clima;
    document.getElementById('w-temp-val').textContent    = w.temp;
    document.getElementById('w-lluvia-val').textContent  = w.lluvia;
    document.getElementById('w-aire-val').textContent    = w.aire;
    var actLabel = document.getElementById('w-activity-name');
    if (actLabel) actLabel.textContent = a.title;
  }

  function getMarkerHtml(a) {
    var labels = { baloncesto: '🏀', running: '🏃', ciclismo: '🚴' };
    return '<div class="map-marker map-marker--' + a.id + '">' + (labels[a.id] || '📍') + '</div>';
  }

  function initMap() {
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
          '<div class="map-popup__badge">' + a.badge + '</div>' +
          '<div class="map-popup__title">' + a.title + '</div>' +
          '<div class="map-popup__desc">' + a.desc + '</div>' +
        '</div>',
        { closeButton: false }
      );

      marker.on('click', function () { setWeather(a); });
    });

    setWeather(activities[1]);
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
    initMap();
    initFilters();
  });

}());
