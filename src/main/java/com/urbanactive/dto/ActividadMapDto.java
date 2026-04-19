package com.urbanactive.dto;

public class ActividadMapDto {

    private String id;
    private double lat;
    private double lng;
    private String icon;
    private String title;
    private String tipoDeporte;
    private WeatherDto weather;
    private int plazasOcupadas;
    private int plazasTotal;
    private String estado;
    private String fechaHora;

    public ActividadMapDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTipoDeporte() {
        return tipoDeporte;
    }

    public void setTipoDeporte(String tipoDeporte) {
        this.tipoDeporte = tipoDeporte;
    }
    public WeatherDto getWeather() {
        return weather;
    }

    public void setWeather(WeatherDto weather) {
        this.weather = weather;
    }

    public static class WeatherDto {
        private String clima;
        private String climaIcon;
        private String temp;
        private String lluvia;
        private String aire;
        private String alerta;

        public String getClima() {
            return clima;
        }

        public void setClima(String clima) {
            this.clima = clima;
        }

        public String getClimaIcon() {
            return climaIcon;
        }

        public void setClimaIcon(String climaIcon) {
            this.climaIcon = climaIcon;
        }

        public String getTemp() {
            return temp;
        }

        public void setTemp(String temp) {
            this.temp = temp;
        }

        public String getLluvia() {
            return lluvia;
        }

        public void setLluvia(String lluvia) {
            this.lluvia = lluvia;
        }

        public String getAire() {
            return aire;
        }

        public void setAire(String aire) {
            this.aire = aire;
        }

        public String getAlerta() {
            return alerta;
        }

        public void setAlerta(String alerta) {
            this.alerta = alerta;
        }
    }

    public int getPlazasOcupadas() {
        return plazasOcupadas;
    }

    public void setPlazasOcupadas(int plazasOcupadas) {
        this.plazasOcupadas = plazasOcupadas;
    }

    public int getPlazasTotal() {
        return plazasTotal;
    }

    public void setPlazasTotal(int plazasTotal) {
        this.plazasTotal = plazasTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }
}
