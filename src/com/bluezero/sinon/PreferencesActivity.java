package com.bluezero.sinon;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

public class PreferencesActivity extends PreferenceActivity {
    private static int preferences = R.xml.preferences;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	AddResourceApi11AndGreater();
        	enableActionBarHomeUp();
        }
        else {
            AddResourceApiLessThan11();
        }
    }

    @SuppressWarnings("deprecation")
    protected void AddResourceApiLessThan11()
    {
        addPreferencesFromResource(preferences);

        Preference sharePref = findPreference("shareApplication");
        setShareIntent(sharePref);

        Preference ratePref = findPreference("rateApplication");
        setRateIntent(ratePref);

        Preference emailPref = findPreference("applicationFeedback");
        setFeedbackIntent(emailPref);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void AddResourceApi11AndGreater()
    {
        PreferencesFragment prefsFragment = new PreferencesFragment();

        getFragmentManager().beginTransaction().replace(android.R.id.content, prefsFragment).commit();
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == android.R.id.home) {
            finish();
    	}
    	
    	return true;
    }
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void enableActionBarHomeUp() {
        ActionBar ab = getActionBar();
        if (ab != null) {
		    ab.setDisplayHomeAsUpEnabled(true);
        }
	}

    private void setShareIntent(Preference pref) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, "Check out this app!");
        share.putExtra(Intent.EXTRA_TEXT, "Check out Trammy Dodger! " + "https://play.google.com/store/apps/details?id=" + getPackageName());
        pref.setIntent(share);
    }

    private void setRateIntent(Preference pref) {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent rate = new Intent(Intent.ACTION_VIEW, uri);
        pref.setIntent(rate);
    }

    private void setFeedbackIntent(Preference pref) {
        Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:contact@bluezero.co.uk"));
        email.putExtra(Intent.EXTRA_SUBJECT, "Trammy Dodger");
        pref.setIntent(email);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public class PreferencesFragment extends PreferenceFragment
    {       
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(PreferencesActivity.preferences);

            Preference sharePref = findPreference("shareApplication");
            setShareIntent(sharePref);

            Preference ratePref = findPreference("rateApplication");
            setRateIntent(ratePref);

            Preference emailPref = findPreference("applicationFeedback");
            setFeedbackIntent(emailPref);
        }
    }
}
