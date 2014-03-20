package com.bluezero.sinon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;

public class WhatsNew {
	public static AlertDialog buildWhatsNewDialog(final Activity parentActivity, final OnClickListener clickListener) {
		AlertDialog dialog = null;
		
    	SharedPreferences prefs = parentActivity.getPreferences(Context.MODE_PRIVATE);
    	int currentVersionNumber = 0;
		
		int savedVersionNumber = prefs.getInt("version", 0);
		
		try {
   	 		PackageInfo pi = parentActivity.getPackageManager().getPackageInfo(parentActivity.getPackageName(), 0);
    	 	currentVersionNumber = pi.versionCode;
   	 	} 
		catch (Exception e) {}
   	 	   	 	
   	 	if (currentVersionNumber > savedVersionNumber) {   	 		
   	 		LayoutInflater inflater = LayoutInflater.from(parentActivity);		
		
	        View view = inflater.inflate(R.layout.whats_new_dialog, null);
	      	
	  	  	Builder builder = new AlertDialog.Builder(parentActivity);
	
		  	builder.setView(view);
		  	builder.setTitle(R.string.dialog_whats_new);
		  	builder.setPositiveButton("OK", clickListener);		  	
  	
		  	Editor editor = prefs.edit();
   	 		
   	 		editor.putInt("version", currentVersionNumber);
   	 		editor.commit();
		  	
		  	dialog = builder.create();   	 		   	 	
   	 	}
   	 	
   	 	return dialog;
    }
}
