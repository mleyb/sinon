package com.bluezero.sinon;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;

import com.microsoft.windowsazure.mobileservices.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;

public class UserInfo {
	public static MobileServiceUser load(Context context) {
		SharedPreferences prefs = getSharedPreferences(context);
		if (prefs.contains("User")) {
			String userId = prefs.getString("User", null);
			if (userId != null) {
				MobileServiceUser user = new MobileServiceUser(userId);	
				user.setAuthenticationToken(prefs.getString("Token", null));
				return user;
			}
		}
		
		return null;
	}
	
	public static void save(Context context, MobileServiceUser user) {
		SharedPreferences prefs = getSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putString("User", user.getUserId());
		editor.putString("Token", user.getAuthenticationToken());
		editor.commit();
	}
	
	public static void clear(Context context) {
		SharedPreferences prefs = getSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putString("User", null);
		editor.putString("Token", null);
		editor.commit();
	}
	
	public static MobileServiceAuthenticationProvider getProvider(Context context) {
		MobileServiceUser user = load(context);		
		if (user.getUserId().startsWith("Facebook")) {
			return MobileServiceAuthenticationProvider.Facebook;
		}
		else if (user.getUserId().startsWith("MicrosoftAccount")) {
			return MobileServiceAuthenticationProvider.MicrosoftAccount;
		}
		if (user.getUserId().startsWith("Twitter")) {
			return MobileServiceAuthenticationProvider.Twitter;
		}
		else {
			return MobileServiceAuthenticationProvider.Google; // default
		}
	}
	
	private static SharedPreferences getSharedPreferences(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return new SecurePreferences(context);
		}
		else {
			return PreferenceManager.getDefaultSharedPreferences(context);
		}
	}
}
