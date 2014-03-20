package com.bluezero.sinon;

import android.app.Activity;
import android.app.Application;

import com.bugsense.trace.BugSenseHandler;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;

public class SinonApplication extends Application {
    private static SinonApplication _instance;

	private Activity _currentActivity;

	private MobileServiceClient _azureClient;

    private LocationHelper _locationHelper;

    public static final SinonApplication getInstance() {
        return _instance;
    }

    public final LocationHelper getLocationHelper() {
        return _locationHelper;
    }

	@Override
	public void onCreate() {
		super.onCreate();			

        _instance = this;

    	BugSenseHandler.initAndStartSession(this, Constants.BUGSENSE_API_KEY);

        _locationHelper = new LocationHelper(this);
        _locationHelper.connect();
	}
	
	@Override
	public void onTerminate() {
        _locationHelper.disconnect();
		BugSenseHandler.closeSession(this);
        super.onTerminate();
	}
	
	public void setCurrentActivity(Activity activity) {
		_currentActivity = activity;
		getServiceClient().setContext(activity);
	}
	
	public Activity getCurrentActivity() {
		return _currentActivity;
	}
	
	public MobileServiceClient getServiceClient() {
		if (_azureClient == null) {			
			final MobileServiceUser user = UserInfo.load(getApplicationContext());	        
	        _azureClient = MobileServiceClientFactory.createAzureClient(getCurrentActivity(), user);        			 
		}
		
		return _azureClient;
	}
}
