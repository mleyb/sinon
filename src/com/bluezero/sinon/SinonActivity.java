package com.bluezero.sinon;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

public abstract class SinonActivity extends FragmentActivity {
	private ProgressDialog _progressDialog;
	
	private boolean _gaEnabled;

	protected MobileServiceClient getServiceClient() {
		return SinonApplication.getInstance().getServiceClient();
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SinonApplication.getInstance().setCurrentActivity(this);
        
        _progressDialog = new ProgressDialog(this);
		_progressDialog.setTitle("Please wait");
		_progressDialog.setMessage("Retrieving information...");
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
  		_gaEnabled = prefs.getBoolean("enableGoogleAnalytics", false); 	
    }

    @Override
    public void onStart() {
      super.onStart();     
      
      	if (_gaEnabled) {  			
  			EasyTracker.getInstance().activityStart(this);
    	}
    }

    @Override
    public void onStop() {
    	super.onStop();     
    	
    	if (_progressDialog.isShowing()) {
    		_progressDialog.dismiss();
    	} 
    	
    	if (_gaEnabled) {  			  		
    		EasyTracker.getInstance().activityStop(this);
    	}
    }
        
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.action_map:        		
    			startActivity(new Intent(this, MapViewerActivity.class));
    			break;
	    	case R.id.action_settings:
	    		startActivity(new Intent(this, PreferencesActivity.class));
	    		break;	    	
	    	case R.id.action_about:
	    		AboutDialog.show(this);
	    		break;
    	}    	
    	
    	return true;
    }	                
    
    protected void configureTitleBar() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String title = prefs.getString("NetworkName", "Welcome");
    	
		TextView textView = (TextView)findViewById(R.id.textViewTitle);
		if (textView != null) {
			textView.setText(title);
		}
    }
    
    protected void showProgress(int id) {
		if (_progressDialog != null && !_progressDialog.isShowing()) {
			_progressDialog.setMessage(getResources().getString(id));
			_progressDialog.show();
		}   
    }
    
    protected void hideProgress() {
		if (_progressDialog != null && _progressDialog.isShowing()) {
			_progressDialog.dismiss();
		}    		
    }

    protected boolean isDataConnectionAvailable() {
        ConnectivityManager mgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = false;
        try {
            connected = (mgr.getActiveNetworkInfo() != null &&
                         mgr.getActiveNetworkInfo().isAvailable() &&
                         mgr.getActiveNetworkInfo().isConnected());
        }
        catch (Exception ex) { }
        return connected;
    }
}
