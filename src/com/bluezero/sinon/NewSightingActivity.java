package com.bluezero.sinon;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bluezero.sinon.models.Sighting;
import com.bluezero.sinon.models.Station;
import com.bugsense.trace.BugSenseHandler;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.QueryOrder;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;

import java.util.List;

public class NewSightingActivity extends SinonActivity {
    private String TAG = this.getClass().getSimpleName();

    private static final int PageSize = 100;

	private MobileServiceClient _azureClient;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _azureClient = MobileServiceClientFactory.createAzureClient(this, UserInfo.load(getApplicationContext()));

        setContentView(R.layout.activity_new_sighting);

        configureActionBar();
        configureTitleBar();

        if (savedInstanceState == null) {
        	initialize();	
        }
                
        ListView listView = (ListView)findViewById(R.id.listViewSelections);
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        		saveSighting((Station)adapterView.getItemAtPosition(position));
        	}
		});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

	@Override        
    protected void onSaveInstanceState(Bundle savedInstanceState) {
       super.onSaveInstanceState(savedInstanceState);            
       // TODO - save list content to the bundle
    }
	
    @Override    
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);     
    	// TODO - pull list content back from the bundle
    	initialize();	
    } 
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem item = menu.findItem(R.id.action_refresh);
    	item.setVisible(false);
    	
    	item = menu.findItem(R.id.action_new_sighting);
    	item.setVisible(false);
    	
    	item = menu.findItem(R.id.action_network);
    	item.setVisible(false);
    	
    	item = menu.findItem(R.id.action_signout);
    	item.setVisible(false);
    	
    	return true;
    }       
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == android.R.id.home) {
    		finish();
            return true;
    	}
    	else {
    		return super.onOptionsItemSelected(item);
    	}
    }	   
	
	public void initialize() {
		showProgress(R.string.progress_retrieving_stations);
		int networkId = PreferenceManager.getDefaultSharedPreferences(this).getInt("NetworkId", 0);
		_azureClient.getTable(Station.class).where().field("NetworkId").eq(networkId).top(PageSize).orderBy("name", QueryOrder.Ascending).execute(new TableQueryCallback<Station>() {
        	@Override
        	public void onCompleted(List<Station> result, int count, Exception exception, ServiceFilterResponse response) {
        		if (exception == null) {
        			StationArrayAdapter adapter = new StationArrayAdapter(NewSightingActivity.this, result);
        			ListView listView = (ListView)findViewById(R.id.listViewSelections); 
        			listView.setAdapter(adapter);
        			hideProgress();
        		}
        		else {
        			BugSenseHandler.sendException(exception);
        			hideProgress();
        			Toast.makeText(NewSightingActivity.this, getResources().getString(R.string.toast_general_error), Toast.LENGTH_SHORT).show();
        			finish();
        		}        		        	
        	}			           	
        });
	}

	private void saveSighting(Station station) {
        if (!isDataConnectionAvailable()) {
            Toast.makeText(this, R.string.toast_no_data_connection, Toast.LENGTH_SHORT);
            return;
        }

        showProgress(R.string.progress_saving_location);

        final Location location = SinonApplication.getInstance().getLocationHelper().getLastLocation();

		Sighting sighting = new Sighting();
		sighting.NetworkId = PreferenceManager.getDefaultSharedPreferences(this).getInt("NetworkId", 0);
		sighting.StationId = station.Id;
		sighting.StationName = station.Name;
		sighting.DateTime = DateTimeUtil.getCurrentDateJSONString();
		sighting.PushChannel = GCMLink.getGCMRegistrationId();
		sighting.Latitude = Double.toString(location.getLatitude());
        sighting.Longitude = Double.toString(location.getLongitude());

		_azureClient.getTable(Sighting.class).insert(sighting, new TableOperationCallback<Sighting>() {
        	public void onCompleted(Sighting entity, Exception exception, ServiceFilterResponse response) {
        		if (exception == null) {
        			Toast.makeText(NewSightingActivity.this, getResources().getString(R.string.progress_sighting_recorded), Toast.LENGTH_SHORT).show();
        			NewSightingActivity.this.setResult(RESULT_OK);
        			finish();
        		} 
        		else {
        			BugSenseHandler.sendException(exception);
        			hideProgress();
        			Toast.makeText(NewSightingActivity.this, getResources().getString(R.string.toast_general_error), Toast.LENGTH_SHORT).show();
        		}
        	}
        });
	}
	
	@SuppressLint("NewApi")
	private void configureActionBar() {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    		ActionBar actionBar = getActionBar();
        	actionBar.setTitle(getResources().getString(R.string.title_new_sighting));    
        	actionBar.setSubtitle(getResources().getString(R.string.title_report_new_sighting));
        	actionBar.setDisplayHomeAsUpEnabled(true);
        }
        else {
            setTitle(getResources().getString(R.string.title_new_sighting));
        }       	
    }
}
