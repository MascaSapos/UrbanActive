package com.urbanactive.dto;

public class ActividadMapDto {

    private double lat;
    private double lng;
    private String icon;
    private String title;

    public ActividadMapDto() {
    }

    public ActividadMapDto(double lat, double lng, String icon, String title) {
        this.lat = lat;
        this.lng = lng;
        this.icon = icon;
        this.title = title;
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
}
