package com.bluezero.sinon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import com.microsoft.windowsazure.mobileservices.*;
import org.apache.http.StatusLine;

public class AuthenticationFilter implements ServiceFilter {
	private static final String TAG = AuthenticationFilter.class.getSimpleName();
	
	private MobileServiceClient _azureClient;
	
	public AuthenticationFilter(MobileServiceClient client) {
		_azureClient = client;
	}
	
	@Override
	public void handleRequest(final ServiceFilterRequest request, final NextServiceFilterCallback nextServiceFilterCallback, final ServiceFilterResponseCallback responseCallback) {
		nextServiceFilterCallback.onNext(request, new ServiceFilterResponseCallback() {				
			@Override
			public void onResponse(ServiceFilterResponse response, Exception exception) {				
				if (exception != null) {
					Log.e(TAG, "onResponse exception: " + exception.getMessage());
				}
				
				StatusLine status = response.getStatus();				
				int statusCode = status.getStatusCode();
				
				Log.i(TAG, String.format("Got status code %d (%s)", statusCode, status.getReasonPhrase()));
				
				if (statusCode == 401) {			
					final Context applicationContext = _azureClient.getContext().getApplicationContext();
					
					// record the provider for re-authentication purposes
					final MobileServiceAuthenticationProvider provider = UserInfo.getProvider(applicationContext);
					
					// log the user out
					_azureClient.logout();
					
					// clear cached user parameters
					UserInfo.clear(applicationContext);
					
					// clear cookies to stop automatic sign-in when the provider web view is presented
					CookieSyncManager.createInstance(applicationContext);
					CookieManager cookieManager = CookieManager.getInstance();
					cookieManager.removeAllCookie();	
					
					// get the current activity for the context so we can show the login dialog
					final Activity currentActivity = SinonApplication.getInstance().getCurrentActivity();
					
					// return a response to the caller (otherwise returning from this method to RequestAsyncTask 
					// will cause a crash).
					responseCallback.onResponse(response, exception);
					
					// show the login dialog on the UI thread
					currentActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							_azureClient.login(provider, new UserAuthenticationCallback() {				
								@Override
								public void onCompleted(MobileServiceUser user, Exception exception, ServiceFilterResponse response) {
									if (exception == null) {
										UserInfo.save(applicationContext, user);
										_azureClient.setCurrentUser(user);	
									} 
									else {
										Log.e(TAG, "User did not re-authenticate successfully after 401");
										
										// return to main activity, within login prompt
										Intent intent = new Intent(currentActivity, MainActivity.class);
										intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
										currentActivity.startActivity(intent);											
									}									
								}
							});									
						}										
					});									
				} 
				else {
					responseCallback.onResponse(response, exception);
				}
			}
		});
	}
}