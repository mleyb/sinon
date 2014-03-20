package com.bluezero.sinon;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bluezero.sinon.models.PushChannel;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;

import java.io.IOException;
import java.sql.Timestamp;

public class GCMLink {
    private static final String TAG = GCMLink.class.getSimpleName();

     // default lifespan (7 days) of a reservation until it is considered expired.
    private static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;

    private Context _context;

    private GoogleCloudMessaging _gcm;

    private static String _gcmRegistrationId;

    public static String getGCMRegistrationId() {
        return _gcmRegistrationId;
    }

    public GCMLink(Context context) {
        _context = context;
        _gcm = GoogleCloudMessaging.getInstance(_context);
    }

    public void initialize() {
        String gcmRegistrationId = getRegistrationId();
        if (gcmRegistrationId == null) {
            registerBackground();
        }
        else {
            _gcmRegistrationId = gcmRegistrationId;
        }
    }

    public void deinitialize() {
        _gcm.close();
    }

    private String getRegistrationId() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        String registrationId = prefs.getString("GCMRegistrationId", null);
        if (registrationId == null) {
            Log.v(TAG, "Registration not found.");
            return null;
        }
        // check if app was updated; if so, it must clear registration id to
        // avoid a race condition if GCM sends a message
        int registeredVersion = prefs.getInt("AppVersion", Integer.MIN_VALUE);
        int currentVersion = getAppVersion(_context);
        if (registeredVersion != currentVersion || isRegistrationExpired()) {
            Log.v(TAG, "App version changed or registration expired.");
            return null;
        }

        return registrationId;
    }

    private void setRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        int appVersion = getAppVersion(context);
        Log.v(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("GCMRegistrationId", regId);
        editor.putInt("AppVersion", appVersion);
        long expirationTime = System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS;

        Log.v(TAG, "Setting registration expiry time to " + new Timestamp(expirationTime));
        editor.putLong("OnServerExpirationTimeMs", expirationTime);
        editor.commit();
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private boolean isRegistrationExpired() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        // checks if the information is not stale
        long expirationTime = prefs.getLong("OnServerExpirationTimeMs", -1);
        return System.currentTimeMillis() > expirationTime;
    }

    private void registerBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    _gcmRegistrationId = _gcm.register(Constants.GCM_SENDER_ID);

                    MobileServiceClient azureClient = MobileServiceClientFactory.createAzureClient(_context, UserInfo.load(_context));

                    MobileServiceTable<PushChannel> registrationTable = azureClient.getTable(PushChannel.class);

                    PushChannel registration = new PushChannel();
                    registration.RegistrationId = _gcmRegistrationId;

                    // register our id with the server
                    registrationTable.insert(registration, new TableOperationCallback<PushChannel>() {
                        @Override
                        public void onCompleted(PushChannel entity, Exception exception, ServiceFilterResponse response) {
                            if (exception == null) {
                                // Save the reg id - no need to register again.
                                setRegistrationId(_context, _gcmRegistrationId);
                                Log.i(TAG, "Device registered, registration id = " + _gcmRegistrationId);
                            }
                            else {
                                Log.e(TAG, "Exception - " + exception.getMessage());
                            }
                        }
                    });
                }
                catch (IOException ex) {
                    Log.e(TAG, "Exception - " + ex.getMessage());
                }
                return null;
            }
        }.execute(null, null, null);
    }
}
