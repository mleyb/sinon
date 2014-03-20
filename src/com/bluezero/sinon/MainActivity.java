package com.bluezero.sinon;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.bluezero.sinon.Constants.RequestCodes;
import com.bluezero.sinon.models.Network;
import com.bluezero.sinon.models.PushChannel;
import com.bluezero.sinon.models.Sighting;
import com.bluezero.sinon.widgets.LoginDialog;
import com.bugsense.trace.BugSenseHandler;
import com.google.android.gcm.GCMRegistrar;
import com.microsoft.windowsazure.mobileservices.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;
import com.microsoft.windowsazure.mobileservices.UserAuthenticationCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends SinonActivity {
	private String TAG = MainActivity.class.getSimpleName();

	private LoginDialog _loginDialog;
	private AlertDialog _whatsNewDialog;

    private GCMLink _gcm;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        _gcm = new GCMLink(getApplicationContext());

        _whatsNewDialog = WhatsNew.buildWhatsNewDialog(this, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
	  			dialog.dismiss();
	  			initialize();
	  		}    		
    	});

        final MobileServiceUser user = UserInfo.load(getApplicationContext());
                                       
        //Callback callback = new Callback() {
            //public boolean handleMessage(Message msg) {
            	if (user == null) {
        	        login(); // login does an implicit begin() for us
                }
                else {
                	setContentView(R.layout.activity_main);         	        
                	configureActionBar();                        	
                	
                	// if savedInstanceState is null then this is a new instance
                	// of the app, otherwise it's being restored from the background
                	if (savedInstanceState == null) {                		
                		beginNew();
                    }
                	else {
                		initialize();
                	}
                }
            	//return true;
            //}
        //};
        
        // seed the database if necessary
        //new DatabaseInitializer(_azureClient).initialize(callback);
    }
    
    @Override
    public void onStop() {
    	super.onStop();
        Log.i(TAG, "onStop()");

        if (_loginDialog != null && _loginDialog.isShowing()) {
    		_loginDialog.dismiss();
    		_loginDialog = null;
    	}  	  
    	
    	if (_whatsNewDialog != null && _whatsNewDialog.isShowing()) {
    		_whatsNewDialog.dismiss();
    		_whatsNewDialog = null;
    	}
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        _gcm.deinitialize();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean isDataConnected = isDataConnectionAvailable();

        switch (item.getItemId()) {
        	case R.id.action_new_sighting:
                if (isDataConnected) {
        		    startActivityForResult(new Intent(this, NewSightingActivity.class), Constants.RequestCodes.NEW_SIGHTING);
                }
        		break;
        	case R.id.action_refresh:
                if (isDataConnected) {
        		    refresh();
                }
                break;
        	case R.id.action_network:
                if (isDataConnected) {
        		    setNetworkChoice(this);
                }
        		break;
	    	case R.id.action_signout:
	    		signOut();
	    		break;
            default:
                return super.onOptionsItemSelected(item);
        }

        if (!isDataConnected) {
            Toast.makeText(MainActivity.this, R.string.toast_no_data_connection, Toast.LENGTH_SHORT);
        }

        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == Activity.RESULT_OK) {
	    	switch (requestCode) {
	    		case RequestCodes.NEW_SIGHTING: {	    			
	    			refresh();
	    			break;
	    		}
	    	}
    	}
    }  

    public void refresh() {
    	initialize();
    }
    
    @SuppressLint("NewApi")
	private void configureActionBar() {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    		ActionBar actionBar = getActionBar();
        	actionBar.setTitle(getResources().getString(R.string.title_sightings));    
        	actionBar.setSubtitle(getResources().getString(R.string.title_todays_sightings));
        }
        else {
            setTitle("Sightings");
        }       	
    }

    private void login() {
    	_loginDialog = new LoginDialog(this);
    	_loginDialog.setTitle(R.string.dialog_sign_in);
    	_loginDialog.setCancelable(true);
    	_loginDialog.setCanceledOnTouchOutside(false);
    	_loginDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
        	@Override
        	public void onCancel(DialogInterface dialog) {
        		MainActivity.this.finish();
        	}
        });               
		
		Button googleLoginButton = (Button)_loginDialog.findViewById(R.id.googleLoginButton);
		googleLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                handleLoginButtonClick(MobileServiceAuthenticationProvider.Google);
			}
		});
		
		Button facebookLoginButton = (Button)_loginDialog.findViewById(R.id.facebookLoginButton);
        facebookLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                handleLoginButtonClick(MobileServiceAuthenticationProvider.Facebook);
			}
		});
        
        Button windowsLoginButton = (Button)_loginDialog.findViewById(R.id.windowsLoginButton);
        windowsLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                handleLoginButtonClick(MobileServiceAuthenticationProvider.MicrosoftAccount);
			}
		});
        
        Button twitterLoginButton = (Button)_loginDialog.findViewById(R.id.twitterLoginButton);
        twitterLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                handleLoginButtonClick(MobileServiceAuthenticationProvider.Twitter);
			}
		});
        	        
		_loginDialog.show();
    }

    private void handleLoginButtonClick(MobileServiceAuthenticationProvider provider) {
        if (isDataConnectionAvailable()) {
            authenticate(MainActivity.this, _loginDialog, provider);
        }
        else {
            Toast.makeText(MainActivity.this, R.string.toast_no_data_connection, Toast.LENGTH_SHORT);
        }
    }

    private void authenticate(final MainActivity activity, final Dialog loginDialog, MobileServiceAuthenticationProvider provider) {
    	getServiceClient().login(provider, new UserAuthenticationCallback() {
			@Override
			public void onCompleted(MobileServiceUser user, Exception exception, ServiceFilterResponse response) {
			    if (exception == null) {
			    	UserInfo.save(getApplicationContext(), user);			    	
			    	getServiceClient().setCurrentUser(user);					    	
			    	loginDialog.dismiss();			    	
			    	setContentView(R.layout.activity_main);			    	
			    	configureActionBar();
			    	configureTitleBar();			    				    
			        			   
			    	// kick things off
			    	activity.beginNew();
			    }
			    else {
			    	if (exception instanceof MobileServiceException && !(exception.getMessage().equals("User Canceled"))) {
			    		BugSenseHandler.sendException(exception);
				    	Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_sign_in_error), Toast.LENGTH_SHORT).show();			    		
			    	}
			    }
			}
        });
    }
    
    public void initialize() {    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	if (!prefs.contains("NetworkId")) {
    		setNetworkChoice(this);
    	}
    	else {    	
    		configureTitleBar();
            if (!isDataConnectionAvailable()) {
                Toast.makeText(MainActivity.this, R.string.toast_no_data_connection, Toast.LENGTH_SHORT);
                return;
            }
	    	showProgress(R.string.progress_updating_sightings);
	    	getServiceClient().getTable(Sighting.class).where().field("networkid").eq(prefs.getInt("NetworkId", 0)).execute(new TableQueryCallback<Sighting>() {
	        	@Override
	        	public void onCompleted(List<Sighting> result, int count, Exception exception, ServiceFilterResponse response) {
	        		if (exception == null) {
                        ArrayList<SightingsListGroup> sightingGroups = SightingsCollator.GetSightingGroups(result);
	        			ExpandableListView list = (ExpandableListView)findViewById(R.id.listView);
	        	        list.setAdapter(new SightingsListAdapter(MainActivity.this, sightingGroups));
                        SinonApplication.getInstance().getLocationHelper().createGeofences(sightingGroups, MainActivity.this.getTransitionPendingIntent());
	        		}
	        		else {
	        			BugSenseHandler.sendException(exception);
	        			Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_general_error), Toast.LENGTH_SHORT).show();
	        		}     
	        		
        			hideProgress();
	        	}			        
	        });
    	}
	}
    
    public void setNetworkChoice(final Context context) {
    	showProgress(R.string.progress_retrieving_networks);
    	
    	String culture = getCultureCode();
    	// force UK for now until there are DE & AUS networks
    	culture = "en-GB";

    	getServiceClient().getTable(Network.class).where().field("CultureCode").eq(culture).execute(new TableQueryCallback<Network>() {
        	@Override
        	public void onCompleted(final List<Network> result, int count, Exception exception, ServiceFilterResponse response) {        		
        		if (exception == null) {
        			ArrayList<String> names = new ArrayList<String>();
        			for (Network network : result) {
        				names.add(network.Name);
        			}
        			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        		    builder.setTitle(R.string.dialog_select_network);
        		    builder.setCancelable(false);
        		    builder.setItems(names.toArray(new CharSequence[names.size()]), new DialogInterface.OnClickListener() {
        		        public void onClick(DialogInterface dialog, int item) {
        		        	Network selectedNetwork = result.get(item);
        		        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        		    		Editor editor = prefs.edit();
        		    		editor.putInt("NetworkId", selectedNetwork.Id);
        		    		editor.putString("NetworkName", selectedNetwork.Name);
        		    		editor.commit();

        		    		dialog.dismiss();

        		    		// refresh data
        		    		initialize();
        		        }
        		    });

        		    AlertDialog dialog = builder.create();

        		    hideProgress();

        		    dialog.show();
        		}
        		else {
        			BugSenseHandler.sendException(exception);
        			hideProgress();
        			Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_general_error), Toast.LENGTH_SHORT).show();
        		}
        	}			        
        });
	}
    
	public void beginNew() {
    	// initialize GCM
        _gcm.initialize();
    	    	
    	if (_whatsNewDialog != null) {
    		_whatsNewDialog.show();
    	}
    	else {    			       
    		initialize();
    	}
   	}
	
	private void signOut() {
		new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.dialog_sign_out)
        .setMessage(R.string.dialog_sign_out_message)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
        	@Override
        	public void onClick(DialogInterface dialog, int which) {
        		// log the user out
				SinonApplication.getInstance().getServiceClient().logout();
				
				// clear cached user parameters
				UserInfo.clear(getApplicationContext());
				
				// clear cookies to stop automatic sign-in when the provider web view is presented
				CookieSyncManager.createInstance(getApplicationContext());
				CookieManager cookieManager = CookieManager.getInstance();
				cookieManager.removeAllCookie();

				// return to main activity, within login prompt
				Intent intent = new Intent(MainActivity.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
        	}
        })
        .setNegativeButton(R.string.no, null)
        .show();
	}
	
	private String getCultureCode() { 
        String language = Locale.getDefault().getISO3Language().substring(0, 2);
        String country = Locale.getDefault().getISO3Country().substring(0, 2);

        String code = String.format("%s-%s", language, country);
        return code;
	}

    private PendingIntent getTransitionPendingIntent() {
        // Create an explicit Intent
        Intent intent = new Intent(this, GeofenceTransitionIntentService.class);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}






