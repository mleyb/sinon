package com.bluezero.sinon;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;

public class LocationHelper implements GooglePlayServicesClient.ConnectionCallbacks,
                                        GooglePlayServicesClient.OnConnectionFailedListener,
                                        LocationClient.OnAddGeofencesResultListener,
                                        LocationClient.OnRemoveGeofencesResultListener {
    private String TAG = this.getClass().getSimpleName();

    private final Context _context;

    private LocationClient _locationClient;

    public LocationHelper(Context context) {
        _context = context;
    }

    public void connect() {
        if (_locationClient == null) {
            _locationClient = new LocationClient(_context, this, this);
            _locationClient.connect();
        }
    }

    public void disconnect() {
        if (_locationClient != null) {
            _locationClient.disconnect();
            _locationClient = null;
        }
    }

    public Location getLastLocation() {
        return _locationClient.getLastLocation();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected");
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "onDisconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "onConnectionFailed");
    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        if (statusCode == LocationStatusCodes.SUCCESS) {
            Log.i(TAG, "onAddGeofencesResult success");
        }
        else if (statusCode == LocationStatusCodes.ERROR) {
            Log.e(TAG, "onAddGeofencesResult error");
        }
        else {
            Log.e(TAG, "onAddGeofencesResult other error (" + Integer.toString(statusCode) + ")");
        }
    }

    @Override
    public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] strings) {

    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {

    }

    public void createGeofences(ArrayList<SightingsListGroup> sightingGroups, PendingIntent intent) {
        ArrayList<Geofence> geofencesToCreate = new ArrayList<Geofence>();
        ArrayList<String> geofencesToRemove = new ArrayList<String>();

        for (SightingsListGroup group : sightingGroups) {
            if (group.Name.equals(SightingsListGroup.Last15MinutesGroupName) ||
                group.Name.equals(SightingsListGroup.Last30MinutesGroupName)) {
                for (SightingsListItem item : group.Items) {
                    SightingGeofence geofence = new SightingGeofence(Integer.toString(item.Id), Double.parseDouble(item.Latitude), Double.parseDouble(item.Longitude), 1500);
                    geofencesToCreate.add(geofence.toGeofence());
                }
            }
            else {
                for (SightingsListItem item : group.Items) {
                    geofencesToRemove.add(item.Name);
                }
            }
        }

        if (geofencesToCreate.size() > 0) {
            _locationClient.addGeofences(geofencesToCreate, intent, this);
        }

        if (geofencesToRemove.size() > 0) {
            _locationClient.removeGeofences(geofencesToRemove, this);
        }
    }
}