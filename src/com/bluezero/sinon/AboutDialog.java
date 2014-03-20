package com.bluezero.sinon;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class AboutDialog {
	public static void show(Context context) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);		
		dialog.setTitle(R.string.app_name);			
		dialog.setContentView(R.layout.about_dialog);
		dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_launcher);		
				
		((TextView)dialog.findViewById(R.id.version)).setText("v" + getVersionName(context));
		((TextView)dialog.findViewById(R.id.copyright)).setText(R.string.app_copyright);				
		((TextView)dialog.findViewById(R.id.link)).setText("www.bluezero.co.uk");
		
		((TextView)dialog.findViewById(R.id.link)).setMovementMethod(LinkMovementMethod.getInstance());
		
		Button dialogButton = (Button)dialog.findViewById(R.id.dialogButtonOK);

		dialogButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();					
			}
		}); 
		
		dialog.show();
    }
	
	private static String getVersionName(Context context) {
		String name = null;
		try
		{
		    name = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		}
		catch (NameNotFoundException e) { }		
		return name;
	}
}
