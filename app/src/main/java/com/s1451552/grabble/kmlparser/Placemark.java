package com.s1451552.grabble.kmlparser;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Class representing placemark used in Google Maps
 * Edited version for Grabble
 */
public class Placemark {

	private String title;
	private String description;
	private LatLng coordinates;

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

    public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

    public LatLng getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(String coordinates) {
        String[] parts = coordinates.split(",");
		this.coordinates = new LatLng(Double.parseDouble(parts[1]), Double.parseDouble(parts[0]));
	}

    @Override
	public String toString() {
        return (this.title + " " + this.description + " " + this.coordinates);
    }

}