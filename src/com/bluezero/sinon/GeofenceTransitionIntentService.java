package com.bluezero.sinon;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

public class GeofenceTransitionIntentService extends IntentService {
    private String TAG = MainActivity.class.getSimpleName();

    public GeofenceTransitionIntentService() {
        super("GeofenceTransitionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!LocationClient.hasError(intent)) {
            int transitionType = LocationClient.getGeofenceTransition(intent);

            //if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
                List<Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);

                String[] triggerIds = new String[triggerList.size()];

                for (int i = 0; i < triggerIds.length; i++) {
                    // Store the Id of each geofence
                    triggerIds[i] = triggerList.get(i).getRequestId();


                }
            }
        }
        else {
            int errorCode = LocationClient.getErrorCode(intent);
            Log.e(TAG, "Location Services error: " + Integer.toString(errorCode));
        }
    }

    private void notifyGeofenceTransitions(List<String> geofenceIds) {
        for (String geofenceId : geofenceIds) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Geofence alert!")
                    .setContentText("Geofence exited! (" + geofenceId + ")");

            Intent resultIntent = new Intent(this, MainActivity.class);

            // ensure that navigating backward from the Activity leads out of
            // the application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

            // add the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);

            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(Constants.NOTIFICATION_ID, builder.build());
        }
    }
}