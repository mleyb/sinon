package com.bluezero.sinon;

import java.util.List;

import android.os.AsyncTask;
import android.os.Handler.Callback;
import android.util.Log;

import com.bluezero.sinon.models.*;
import com.bluezero.sinon.networks.*;
import com.bugsense.trace.BugSenseHandler;
import com.microsoft.windowsazure.mobileservices.*;

public class DatabaseInitializer {
	private static final String TAG = DatabaseInitializer.class.getSimpleName();
	
	private MobileServiceClient _azureClient;
	private MobileServiceTable<Network> _networkTable;
	private MobileServiceTable<Station> _stationTable; 
	
	public DatabaseInitializer(MobileServiceClient client) {
		_azureClient = client;
		
		_networkTable = _azureClient.getTable(Network.class);
		_stationTable = _azureClient.getTable(Station.class);
	}
	
	public void initialize(final Callback completed) {
		_networkTable.where().execute(new TableQueryCallback<Network>() {
        	@Override
        	public void onCompleted(List<Network> result, int count, Exception exception, ServiceFilterResponse response) {
        		if (exception == null) {
        			//if (result.size() == 0) {
        				new SeedDatabaseAsyncTask(completed).execute();
        			//}
        			//else {
//        				completed.handleMessage(null);
  //      			}
        		}
        		else {
        			completed.handleMessage(null);
        		}
        	}			        
        });
	}
		
	private class SeedDatabaseAsyncTask extends AsyncTask<Void, Void, Void> {
		private Callback _onCompleted;
		
		public SeedDatabaseAsyncTask(Callback completed) {
			_onCompleted = completed;
		}
		
		@Override
        protected void onPreExecute() {
			// Not used
        }
        
        @Override
        protected Void doInBackground(Void... params) {     
        	createManchesterNetwork();		
    		createTyneAndWearNetwork();
    		createNottinghamNetwork();
        	createSheffieldNetwork();
        	return null;
        }      

        @Override
        protected void onProgressUpdate(Void... values) {
        	// Not used
        }
        
        @Override
        protected void onPostExecute(Void result) {
        	_onCompleted.handleMessage(null);
        }        
	}   
	
	private void createManchesterNetwork() {
		Network network = new Network();
		network.Name = ManchesterNetwork.Name;	
		network.CultureCode = ManchesterNetwork.CultureCode;
		createNetwork(network, ManchesterNetwork.Stations);
	}
	
	private void createTyneAndWearNetwork() {
		Network network = new Network();
		network.Name = TyneAndWearNetwork.Name;
		network.CultureCode = TyneAndWearNetwork.CultureCode;
		createNetwork(network, TyneAndWearNetwork.Stations);
	}
	
	private void createNottinghamNetwork() {
		Network network = new Network();
		network.Name = NottinghamNetwork.Name;	
		network.CultureCode = NottinghamNetwork.CultureCode;
		createNetwork(network, NottinghamNetwork.Stations);
	}
	
	private void createSheffieldNetwork() {
		Network network = new Network();
		network.Name = SheffieldNetwork.Name;	
		network.CultureCode = SheffieldNetwork.CultureCode;
		createNetwork(network, SheffieldNetwork.Stations);
	}
	
	private void createNetwork(Network network, final String[] stations) {
		_networkTable.insert(network, new TableOperationCallback<Network>() {
        	public void onCompleted(Network entity, Exception exception, ServiceFilterResponse response) {
        		if (exception == null) {
        			for (int i = 0; i < stations.length; i++) {      				
        				createStation(entity.Id, stations[i]);
        				pause(500);
        			}
        		} 
        		else {
        			BugSenseHandler.sendException(exception);
        			Log.e(TAG, "Exception on Network insert", exception);
        		}
        	}
        });
	}
	
	private void createStation(int networkId, String name) {
		Station station = new Station();
		station.NetworkId = networkId;
		station.Name = name;
		
		_stationTable.insert(station, new TableOperationCallback<Station>() {
        	public void onCompleted(Station entity, Exception exception, ServiceFilterResponse response) {
        		if (exception == null) {
        		} 
        		else {
        			BugSenseHandler.sendException(exception);
        			Log.e(TAG, "Exception on Station insert", exception);
        		}
        	}
        });
	}
	
	private void pause(int ms) {
		try {
			Thread.sleep(ms);
		} 
		catch (InterruptedException e) {
			BugSenseHandler.sendException(e);
			Log.e(TAG, "InterruptedException on wait", e);
		}
	}
}
