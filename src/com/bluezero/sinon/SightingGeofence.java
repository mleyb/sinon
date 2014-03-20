package com.bluezero.sinon;

import com.google.android.gms.location.Geofence;

/**
 * Created by mark.leybourne on 10/06/13.
 */
public class SightingGeofence {
    public String Id;
    public double Latitude;
    public double Longitude;
    public float Radius;

    public SightingGeofence(String geofenceId, double latitude, double longitude, float radius) {
        Id = geofenceId;
        Latitude = latitude;
        Longitude = longitude;
        Radius = radius;
    }

    public Geofence toGeofence() {
        return new Geofence.Builder()
                .setRequestId(Id)
                //.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .setCircularRegion(Latitude, Longitude, Radius)
                .setExpirationDuration(Constants.Geofence.GEOFENCE_EXPIRATION_TIME)
                .build();
    }
}
