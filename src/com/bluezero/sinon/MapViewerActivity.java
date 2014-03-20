package com.bluezero.sinon;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bluezero.sinon.networks.*;
 
public class MapViewerActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.map);

	    WebView webView =(WebView)findViewById(R.id.webView);

	    webView.setWebViewClient(new WebViewClient());          
	    webView.getSettings().setBuiltInZoomControls(true);	   	   
	    webView.getSettings().setLoadWithOverviewMode(true);
	    webView.getSettings().setUseWideViewPort(true);
	    webView.setInitialScale(30);
	    
	    String html = null;
	    
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    final int networkId = prefs.getInt("NetworkId", 0);
	    
	    switch (networkId) {
	    	case ManchesterNetwork.Id:
	    		html = "<body><img src=\"manchester.jpg\"/></body>";
	    		break;
	    	case TyneAndWearNetwork.Id:
	    		html = "<body><img src=\"tyneandwear.jpg\"/></body>";
	    		break;
	    	case NottinghamNetwork.Id:
	    		html = "<body><img src=\"nottingham.jpg\"/></body>";
	    		break;
	    	case SheffieldNetwork.Id:
	    		html = "<body><img src=\"sheffield.png\"/></body>";
	    		break;
	    }

	    webView.loadDataWithBaseURL("file:///android_asset/", html , "text/html", "utf-8", null);
	}
}
