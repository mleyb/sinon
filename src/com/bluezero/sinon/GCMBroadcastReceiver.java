package com.bluezero.sinon;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;

public class GCMBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = GCMBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {

        }
        else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            //sendNotification("Deleted messages on server: " + intent.getExtras().toString());
        }
        else {
            notify(context, intent);
        }

        setResultCode(Activity.RESULT_OK);
    }

    private void notify(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean notificationsEnabled = prefs.getBoolean("enableNotifications", false);

        if (notificationsEnabled) {
            int networkId;
            String station;
            String gcmRegistrationId;

            try {
                JSONObject message = new JSONObject(intent.getExtras().getString("message"));
                networkId = Integer.parseInt(message.getString("network"));
                station = message.getString("station");
            }
            catch (Exception ex) {
                BugSenseHandler.sendException(ex);
                Log.e(TAG, "Exception - " + ex.getMessage());
                return;
            }

            // only notify if this sighting relates to the current network
            if (networkId == prefs.getInt("NetworkId", 0)) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(context.getResources().getString(R.string.notification_new_sighting))
                        .setContentText(station);

                Intent resultIntent = new Intent(context, MainActivity.class);

                // ensure that navigating backward from the Activity leads out of
                // the application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

                // add the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainActivity.class);

                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultPendingIntent);

                NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(Constants.NOTIFICATION_ID, builder.build());
            }
        }
    }
}
